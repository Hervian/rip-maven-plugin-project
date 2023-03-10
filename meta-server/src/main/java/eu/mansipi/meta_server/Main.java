package eu.mansipi.meta_server;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//TODO: Add Spring circuit breaking. https://arnoldgalovics.com/resilience4j-webclient/  https://spring.io/projects/spring-cloud-circuitbreaker
@SpringBootApplication//(scanBasePackages = "com.example.java_17_server")
@EnableCaching
@Slf4j
public class Main {

  public static void main(String[] args) {
    System.out.println(CacheEventLogger.class.getName());
    ConfigurableApplicationContext applicationContext = SpringApplication.run(Main.class, args);
    List<String> mansipiResources = Arrays.stream(applicationContext.getBeanDefinitionNames()).map(e -> applicationContext.getBean(e).getClass().getName()).filter(str -> str.startsWith("eu")).collect(Collectors.toList());
    mansipiResources.forEach(e -> System.out.println(e));
  }

  public static class CacheEventLogger
      implements CacheEventListener<Object, Object> {

    // ...

    @Override
    public void onEvent(
        CacheEvent<? extends Object, ? extends Object> cacheEvent) {
      log.info(cacheEvent.toString(), cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
  }

}
