package eu.mansipi.meta_server.workinprogress;

import eu.mansipi.meta_server.config.ResilienceConfig;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.cache.Cache;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A resilience4j based aspect for intercepting outgoing REST requests made via the auto-generated Rest class's
 * apis.
 * See https://resilience4j.readme.io/docs/getting-started-3
 * See https://www.baeldung.com/resilience4j
 *
 * @deprecated deprecated in the sense that this was a proof of concept showing that the client api calls can be intercepted and resiliency features added.
 * However, determining which default resilience behavior to add is difficult, for which reason this work in progress is put on hold.
 * Please note that the general idea was as follows:
 * A) in the rip-maven-plugin ClientApiConfig.ftl special resilient apis are made available. The code is pt outcommented.
 * Those resilient apis are annotated with a special annotation which this aspect
 * could be modified to scan for.
 * One should test via this aspect that the feature enriching logic works, after which
 * the code should be added to the compiling project, i.e. the aspect should be moved to rip-maven-plugin
 * and perhaps added to the compiling project by adding it to a template file such as to
 * compile it similar to ClientApiConfig.ftl and Rest.ftl.
 */
//TODO: autocreate from rip plugin
  @Deprecated
@Slf4j
//@Aspect //Outcommented for now. Ideally we created a variant of the autogenerated Rest class called something a la ResilientRest, which added resiliency features to each call. Not simple to do though
@Component //TODO: Add some conditional on? A la https://github.com/resilience4j/resilience4j/blob/88c75c1fdd142e65443a8a028a053f252945800e/resilience4j-spring-boot2/src/main/java/io/github/resilience4j/circuitbreaker/autoconfigure/AbstractCircuitBreakerConfigurationOnMissingBean.java#L39
public class RestRequestAspect {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestRequestAspect.class);

  private Map<String, Optional<Cache<Object, ?>>> caches = new ConcurrentHashMap<>();

  @Autowired //TODO: Prefer @javax.inject.Inject if present on classpath
  private CircuitBreakerRegistry circuitBreakerRegistry;

  @Autowired //TODO: Prefer @javax.inject.Inject if present on classpath
  private RetryRegistry retryRegistry;


  @Pointcut("execution(public * *(..))")
  public void publicMethod() {}

  /* eu.mansipi.random_facts_server APIs */
  @Pointcut("execution(public * eu.mansipi.random_facts_server.FactsApi.*(..))")
  public void factsApiPointcut() {}

  /* eu.mansipi.jokes_server APIs */
  @Pointcut("execution(public * eu.mansipi.jokes_server.JokesApi.*(..))")
  public void jokesApiPointcut() {}

  @Pointcut("execution(public * eu.mansipi.jokes_server.TimeApi.*(..))")
  public void timeApiPointcut() {}

  @Pointcut("execution(public * eu.mansipi.jokes_server.SickServerApi.*(..))")
  public void sickServerApiPointcut() {}


  @Value("${spring.cache.jcache.config}") //TODO: Prefer @javax.inject.Named if present on classpath
  private String jCacheConfig;

  //The created Decorators does not seem to be cached by the resilience framework upon creation. So we maintain our own map.
  Map<Method, DecoratedAndExecutable> decoratedMethods = new ConcurrentHashMap<>();

  @Autowired
  private ResilienceConfig resilienceConfig;

  //TODO: extend documentation such as to make it clear that the project implementing the plugin must have spring aop on the classpath.
  //<!--to import resilience4j decorators and circuit breaker-->
  //	<dependency>
  //		<groupId>io.github.resilience4j</groupId>
  //		<artifactId>resilience4j-all</artifactId>
  //		<version>1.6.1</version>
  //	</dependency>
  //https://stackoverflow.com/a/2522821/6095334
  @Around("publicMethod() && (jokesApiPointcut() || timeApiPointcut() || sickServerApiPointcut())")
  public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable { //TODO: No need to make this an aspect. Should be a postconstruct or static initializer
    String server = "eu.mansipi.jokes_server"; //Generated value based on the <GROUP-ID>.<ARTIFACT-ID % CLIENT> of the compilation unit of the jokesApi and TimeApi
    MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
    String fqcn = methodSignature.getDeclaringTypeName(); //fx: "eu.mansipi.random_facts_server.FactsApi"
    String methodName = methodSignature.getName();
    String args = String.join(",", Arrays.stream(methodSignature.getParameterTypes()).map(Class::getName).collect(Collectors.toList()));
    String methodIdentifier = fqcn.replace("$", ".") + "." + methodName + "(" + args + ")";

    Method method = methodSignature.getMethod();
    if (decoratedMethods.containsKey(method)) {
      return decoratedMethods.get(method).execute();
    }

    ResilienceConfig.ResilienceFeatureInput resilienceFeatureInput = ResilienceConfig.ResilienceFeatureInput
        .builder()
        .jarIdentifier(server)
        .fullyQualifiedClassName(fqcn)
        .methodIdentifier(methodIdentifier)
        .build();

    //TODO:
    //   If spring-cloud-circuitbreaker is on classpath use that project's API
    //     CircuitBreaker cb = Resilience4JCircuitBreakerFactory.create("group", "instance") // https://docs.spring.io/spring-cloud-circuitbreaker/docs/current/reference/html/#usage-documentation
    //   else
    //     use resilienceConfig.createResilienceFeature
    CircuitBreaker circuitBreaker = resilienceConfig.createResilienceFeature(circuitBreakerRegistry::getConfiguration, circuitBreakerRegistry::circuitBreaker, resilienceFeatureInput);

    circuitBreaker.getEventPublisher().onEvent(event -> { //TODO: move to resilienceConfig and use log.info instead of sout
      System.out.println("State change " + event);
    });

    //TODO: Only add Retry to GET operations, to minimize risk that endpoint is not idempotent. Consider making the list of REST operations to decorate with Retry configurable with GET being default.
    // A) Make rip-maven-plugin include swagger.json file in generated client.
    // B) Parse the doc and infer info about which endpoints are GET requests... parser: https://stackoverflow.com/a/68025034/6095334
    // OR simply disable retry by default, unless a custom config for given endpoint is detected. By far the easiest...
    Retry retry = resilienceConfig.createResilienceFeature(retryRegistry::getConfiguration, retryRegistry::retry, resilienceFeatureInput);

    retry.getEventPublisher().onEvent(event -> { //TODO: move to resilienceConfig and use log.info instead of sout
      System.out.println("State change " + event);
    });

    Bulkhead bulkhead = null;
    RateLimiter rateLimiter = null;

    String cacheName = methodIdentifier; //fx eu.mansipi.random_facts_server.FactsApi.getFact(java.lang.Integer)
    System.out.println("Inferred cachename for outgoing http rest call: " + cacheName);
    Optional<Cache<Object, ?>> resilienceCache = getCache(cacheName);


    //TODO: Add below to cache and retrieve it from cache. No: Cache should be a boolean: hasBeenDecorated
    Object returnValue = null;

    try {
      DecoratedAndExecutable decoratedAndExecutable = createDecorateSupplier(
          rethrowSupplier(proceedingJoinPoint::proceed),
          this::fallback,
          circuitBreaker,
          bulkhead,
          retry,
          resilienceCache,
          cacheName,
          rateLimiter
      );
      decoratedMethods.put(method, decoratedAndExecutable);
      returnValue = decoratedAndExecutable.execute();

    } catch (Exception e) {
      returnValue = e;
      throw e;
    }finally {
      LOGGER.info("Around after class - {}, method - {}, returns - {}", proceedingJoinPoint.getSignature().getDeclaringType().getName(), proceedingJoinPoint.getSignature().getName(), returnValue);
    }
    return returnValue;
  }

  private Optional<Cache<Object, ?>> getCache(String cacheName) throws URISyntaxException {
    Optional<Cache<Object, ?>> resilienceCache = Optional.empty();
    if (caches.containsKey(cacheName)){
      resilienceCache = caches.get(cacheName);
    } else {
      try {
        CachingProvider cachingProvider = Caching.getCachingProvider();//ClassLoaders$AppClassLoader vs TomcatEmbeddedWebappClassLoader  https://github.com/javamelody/javamelody/issues/834
        System.out.println("cachingProvider exists? " + cachingProvider!=null);
        System.out.println("Getting cacheManager with URI of configured jcache.config ="+jCacheConfig);
        CacheManager cacheManager = cachingProvider.getCacheManager(new URI(jCacheConfig), null);
        System.out.println("cacheManager exists? " + cacheManager!=null);
        if (cacheManager!=null) {
          cacheManager.getCacheNames().forEach(e -> System.out.println(e));
        }
        javax.cache.Cache<Object, ?> cache = cacheManager.getCache(cacheName);
        resilienceCache = cache==null ? Optional.empty() : Optional.of(Cache.of(cache));
        System.out.println(resilienceCache.isPresent() ? "existing cache found with name " + cacheName : "No cache configured for cacheName = " + cacheName);
        caches.put(cacheName, resilienceCache);
      } catch (CacheException e) {
        //No cache exists for given method. No problem, that just means that the user of this library does not want to cache the responses of those outgoing calls.
        System.out.println("No cache exists for given method ("+ cacheName +"). No problem, that just means that the user of this library does not want to cache the responses of those outgoing calls.");
        caches.put(cacheName, Optional.empty());
      }
    }
    return resilienceCache;
  }

  private <T> DecoratedAndExecutable createDecorateSupplier(Supplier<T> supplier, Function<Throwable, T> fallback, CircuitBreaker circuitBreaker, Bulkhead bulkhead, Retry retry, Optional<Cache<Object, ?>> cache, Object cacheKey, RateLimiter rateLimiter) throws Throwable {
    /**
     * NB: Decorators are applied in the order of the builder chain. See https://javadoc.io/static/io.github.resilience4j/resilience4j-all/1.7.1/io/github/resilience4j/decorators/Decorators.html
     */
    Decorators.DecorateSupplier<T> decorateSupplier= Decorators.ofSupplier(supplier)
        .withRetry(retry)
        .withCircuitBreaker(circuitBreaker) //See also https://resilience4j.readme.io/docs/examples-1#decorate-mono-or-flux-with-a-circuitbreaker
        //.withBulkhead(bulkhead)
        // .withRateLimiter(rateLimiter)//https://resilience4j.readme.io/docs/ratelimiter
        .withFallback(fallback); //TODO: works! - but does the fallback prevent circuitbreaker logic?
    //TODO: make use of Semaphore/ThreadPoolBulkHead?
    //TODO: make use of TimeLimiter? See methods on Decorator for how one can set it: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-all/src/main/java/io/github/resilience4j/decorators/Decorators.java
        /*.withThreadPoolBulkhead()
        .withTimeLimiter()*/
    //.withFallback((Throwable ex) -> {throw new RestRequestAspectException((String) cacheKey, ex);})
    //addFallback(decorateSupplier, (Throwable ex) -> {throw new RuntimeException(ex);});
    return DecoratedAndExecutable
        .builder()
        .decorateSupplier(decorateSupplier)
        .cacheKey(cache.isPresent() ? cacheKey : null)
        .decorateFunction(cache.isPresent() ? decorateSupplier.withCache((Cache<Object, T>)cache.get()) : null)
        .build();
  }

  @Builder //TODO: Create cache of these and pull from here if exists.
  public static class DecoratedAndExecutable<K, T> {
    private Object cacheKey;
    private Decorators.DecorateSupplier<?> decorateSupplier;
    private Decorators.DecorateFunction<Object, ?> decorateFunction;
    public Object execute() {
      return cacheKey==null ? decorateSupplier.get() : decorateFunction.apply(cacheKey);
    }
  }

  //TODO: not here, but when using the fallback: each decorator (timellimiter, ratelimiter etc) can have its own fallback. See useful link: https://github.com/resilience4j/resilience4j/issues/902
  private String fallback(Throwable ex) { //TODO: what default fallback to implement? Can RFC7807 somehow be supported? See fx .of https://blog.codecentric.de/rfc-7807-problem-details-with-spring-boot-and-jax-rs#problem-detail-builder which references this implementation:  https://github.com/t1/problem-details/tree/trunk/api
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    throw new FallbackException(ex);
  }

  public static class FallbackException extends RuntimeException {
    public FallbackException(Throwable t) {
      super(t);
    }
  }

  public static class RestRequestAspectException extends RuntimeException {
    public RestRequestAspectException(String methodName, Throwable throwable) {
      super(methodName, throwable);
    }
  }

  @FunctionalInterface
  public interface SupplierWithException<T, E extends Exception> {
    T get() throws Throwable;
  }

  public static <T, E extends Exception> Supplier<T> rethrowSupplier(SupplierWithException<T, E> supplier) {
    return () -> {
      try {
        T t = supplier.get();
        LOGGER.info("result: " + t);
        return t;
      } catch (Throwable exception) {
        throwAsUnchecked(exception);
        return null;
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <E extends Throwable> void throwAsUnchecked(Throwable exception) throws E {
    throw (E) exception;
  }

}
