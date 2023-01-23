package eu.mansipi.webclient_server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication//(scanBasePackages = "com.example.java_17_server")
public class Main {

  public static void main(String[] args) {
    ConfigurableApplicationContext applicationContext = SpringApplication.run(Main.class, args);
    List<String> mansipiResources = Arrays.stream(applicationContext.getBeanDefinitionNames()).map(e -> applicationContext.getBean(e).getClass().getName()).filter(str -> str.startsWith("eu")).collect(Collectors.toList());
    mansipiResources.forEach(e -> System.out.println(e));

    /*Arrays.stream(Package.getPackages()).forEach(e-> System.out.println(e));*/
  }

}
