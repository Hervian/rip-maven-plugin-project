package eu.mansipi.feign_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.net.URLClassLoader;

@SpringBootApplication//(scanBasePackages = "com.example.java_17_server")
public class Main {

  public static void main(String[] args) {

    ConfigurableApplicationContext applicationContext = SpringApplication.run(Main.class, args);
  }

}
