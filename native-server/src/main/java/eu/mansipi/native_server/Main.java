package eu.mansipi.native_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication//(scanBasePackages = "com.example.java_17_server")
public class Main {

  public static void main(String[] args) {
    ConfigurableApplicationContext applicationContext = SpringApplication.run(Main.class, args);
  }

}
