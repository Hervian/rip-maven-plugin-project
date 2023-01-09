package eu.mansipi.webclient_server.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Random;

@Tag(name="facts", description = "API for random facts retrival...")
@RestController
@RequestMapping("/facts")
public class RandomFactsController {

  @Operation
  @GetMapping("/random")
  //@Produces(MediaType.TEXT_PLAIN)
  public String getRandomFact(){
    int i = new Random().nextInt(3);
    switch (i){
      case 0:  return Mono.just("Fisk er dyr, men ikke alle dyr er fisk.").block();
      case 1:  return Mono.just("Man har fødselsdag een gang om året.").block();
      case 2:  return Mono.just("Man lever kun een gang.").block();
      default: return Mono.just("Non existing fact chosen").block();
    }
  }

}
