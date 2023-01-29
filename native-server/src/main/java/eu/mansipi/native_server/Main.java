package eu.mansipi.native_server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@OpenAPIDefinition(
    servers = @Server(url="/"),
    info = @Info(
        description = "This server demonstrates that:" +
            "<ul>" +
            "<li>A) one can override the default created OpenApiDefinition by explicitly declaring one. " +
            "<li>B) one can use other plugins/dependencies to generate the openapi doc and swagger-ui and simply let the rip-maven-plugin generated the Rest class and the client(s) and run a breaking changes check." +
            "</ul><br>",
        title = "Native-server (called so, because the meta-server project generates a Java 11+ native http client for this server.)",
        version = "1.1"))
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    ConfigurableApplicationContext applicationContext = SpringApplication.run(Main.class, args);
  }

}
