package eu.mansipi.meta_server.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

//TODO: delete me. Resilience features are best added by the caller by wrapping the rest call in an annotated method.
@Configuration
public class ResilienceConfig {

  @Autowired(required = false)
  private CircuitBreakerConfig globalCircuitBreakerConfig = null;

  // Global config: https://docs.spring.io/spring-cloud-circuitbreaker/docs/current/reference/html/spring-cloud-circuitbreaker-resilience4j.html
  //On potential problems creating a new registry: https://github.com/resilience4j/resilience4j/issues/843 ("Like @RobWin indicated, I Autowired the CircuitBreakerRegistry instead of creating my own through CircuitBreakerRegistry.of(config). This allowed me to use registry.circuitBreaker(name, config) to add new circuit breakers with my custom configurations in my code without any yaml entries.")
  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() { // = globalCircuitBreakerConfig==null ? CircuitBreakerRegistry.ofDefaults() : CircuitBreakerRegistry.of(globalCircuitBreakerConfig);
    return globalCircuitBreakerConfig==null
        ? CircuitBreakerRegistry.ofDefaults() //I assume that any properties based CircuitBreaker config may affect this calls, i.e. that a custom global default may have been set that way. How? This call calls configs.getOrDefault("default"), i.e. if a config named "default" is detected that will be used.
        : CircuitBreakerRegistry.of(globalCircuitBreakerConfig);
  }

  /*public CircuitBreaker createCircuitBreaker() {
    return createResilienceFeature(circuitBreakerRegistry::getConfiguration, )
  }*/


  @Autowired(required = false)
  private RetryConfig globalRetryConfig = null;

  /*@Autowired
  private List<RetryConfig> retryConfigList;
*/
  //@Bean
  public RetryRegistry retryRegistry() {
    return globalRetryConfig==null// || retryConfigList.isEmpty()
        ? RetryRegistry.ofDefaults()
        : RetryRegistry.of(globalRetryConfig);
       // ? RetryRegistry.of(RetryConfig.custom().maxAttempts(1).build()) //Register a no-op global default Retry since Retries really is something the developers must actively/consciously configure
        //: RetryRegistry.of(getCustomizedRetryConfig());
  }

 /* private RetryConfig getCustomizedRetryConfig() {
    *
     * If spring-cloud-circuitbreaker is on classpath use that projects API
     *    CircuitBreaker cb = Resilience4JCircuitBreakerFactory.create("group", "instance")
     * java config:
     *    if retryConfigList contains a RetryConfig bean that is defined in the jar being compiled, then use that
     *    if retryConfigList contains a RetryConfig bean that is marked as @Primary use that
     *    sort the retryConfigList and return the lexicographically latest (i.e. prioritize "retryConfig3" over "retryConfig2"

    if RetryRegistry.ofDefaults()
  }*/

  public <T> T createResilienceFeature(Function<String, Optional<?>> configurationProvider, BiFunction<String, String, T> resilienceFeatureProvider, ResilienceFeatureInput resilienceFeatureInput) {
   /* *
     * It should be possible to create/override a resilience feature (fx a circuit breaker) for:
     * - the specific endpoint (FQMN). Fx eu.mansipi.jokes_and_facts_server.JokesApi.getJoke(java.lang.int)
     * - the spefic API (FQCN). Fx: eu.mansipi.jokes_and_facts_server.JokesApi
     * - all the apis of a specific server (groupId:artifactId of client library). eu.mansipi.jokes_and_facts_server
     * - all the apis of all the servers using a custom global configuration.
     * - to simply use the default, global configuration as defined by the resilience4j jars.
*/
    System.out.println(resilienceFeatureInput);
    if (configurationProvider.apply(resilienceFeatureInput.getMethodIdentifier()).isPresent()){ //is endpoint specific circuit breaker configured? FQMN
      return resilienceFeatureProvider.apply(resilienceFeatureInput.getMethodIdentifier(), resilienceFeatureInput.getMethodIdentifier());
    } else if (configurationProvider.apply(resilienceFeatureInput.fullyQualifiedClassName).isPresent()){ //is api level circuit breaker configured? FQCN
      return resilienceFeatureProvider.apply(resilienceFeatureInput.getFullyQualifiedClassName(), resilienceFeatureInput.getFullyQualifiedClassName());
    } else if (configurationProvider.apply(resilienceFeatureInput.getJarIdentifier()).isPresent()){ //is server wide circuit breaker configured? groupId:artifactId of client library
      return resilienceFeatureProvider.apply(resilienceFeatureInput.getJarIdentifier(), resilienceFeatureInput.getJarIdentifier());
    } else { // use global default circuit breaker configuration (which may either be resilience4j's conf OR a custom global conf (See https://resilience4j.readme.io/docs)
      return resilienceFeatureProvider.apply(resilienceFeatureInput.jarIdentifier, "default");
    }
  }

  @Builder
  @Data
  public static class ResilienceFeatureInput {
    private String methodIdentifier; // fullyQualifiedClassName + method name + commaseparated list of argument types.
    private String fullyQualifiedClassName;
    private String jarIdentifier; //groupId.artifactId
  }

/*CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .slowCallRateThreshold(50)
        .waitDurationInOpenState(Duration.ofMillis(100000))
        .slowCallDurationThreshold(Duration.ofSeconds(2))
        .permittedNumberOfCallsInHalfOpenState(3)
        .minimumNumberOfCalls(10)
        //.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
        .slidingWindowSize(5)
        //.recordException(e -> INTERNAL_SERVER_ERROR
         //   .equals(getResponse().getStatus()))
        //.recordExceptions(IOException.class, TimeoutException.class)
        //.ignoreExceptions(BusinessException.class, OtherBusinessException.class)
        .build();*/

}
