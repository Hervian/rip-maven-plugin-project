package eu.mansipi.meta_server.config;

/*import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;*/

/**
 * The Spring-Jersey integration can be done in many ways it seems, and some versions of Spring and Jersey, respectively,
 * have had problems for some of the approaches.
 *
 * The traditional approach is to fx call this.packages(...) to feed the Jersey ResourceConfig with the resources, but
 * for some versions this did not work when the app was actually run as a fat jar. It may have been fixed.
 *
 * Another approach is to use the jax-rs standard "Feature" as described in this SO answer: https://stackoverflow.com/a/59604063/6095334
 *
 * A third approach is the one below, using Springs scanning library to find all the annotated classes and then
 * registering those classes to Jersey.
 *
 * TODO: The best approach would probably be to not scan the classpath but instead access Springs context and only add those
 * beans that exists in the context AND which is annotated with @Path
 *
 * Testing: http://localhost:8080/doc/swagger/swagger.json
 */
/*@Component*/
public class JerseyConfig //extends ResourceConfig implements ApplicationListener<ContextRefreshedEvent> {
{
  /*@Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext appContext = event.getApplicationContext();
    Set<Class<?>> resources =
        Arrays.stream(appContext.getBeanDefinitionNames())
            .map(e -> appContext.getBean(e).getClass())
            .filter(e -> isJaxRsResource(e))
            .collect(Collectors.toSet());
    System.out.println("The following Spring managed beans will be registered with Jersey: ");
    resources.stream().forEach(e -> System.out.println(e));
    registerClasses(resources);
  }*/
  /*@Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext appContext = event.getApplicationContext();
    Set<Object> resources =
        Arrays.stream(appContext.getBeanDefinitionNames())
            //.map(e -> appContext.getBean(e).getClass())
            .filter(e -> isJaxRsResource(e.getClass()))
            .collect(Collectors.toSet());
    System.out.println("The following Spring managed beans will be registered with Jersey: ");
    resources.stream().forEach(e -> System.out.println(e));
    resources.stream().forEach(e -> register(e));
    //register(resources);
  }


  private boolean isJaxRsResource(Class<?> clazz) {
    Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
    for (Annotation annotation: declaredAnnotations){
      if (annotation.annotationType()==Path.class || annotation.annotationType()==Provider.class){
        return true;
      }
    }
    return false;
  }
*/
}