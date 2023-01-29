package eu.mansipi.webclient_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    ConfigurableApplicationContext applicationContext = SpringApplication.run(Main.class, args);
    List<String> mansipiResources = Arrays.stream(applicationContext.getBeanDefinitionNames()).map(e -> applicationContext.getBean(e).getClass().getName()).filter(str -> str.startsWith("eu")).collect(Collectors.toList());
    mansipiResources.forEach(e -> System.out.println(e));

    /*Arrays.stream(Package.getPackages()).forEach(e-> System.out.println(e));*/
  }

}
