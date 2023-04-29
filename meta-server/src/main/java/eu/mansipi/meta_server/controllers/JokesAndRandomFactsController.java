package eu.mansipi.meta_server.controllers;

import eu.mansipi.meta_server.controllers.rest.Rest;
import eu.mansipi.native_server.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@Tag(name="meta", description = "API for calling other servers...")
@RequestMapping("/meta")
public class JokesAndRandomFactsController {

  @Operation
  @GetMapping(("callAllServers"))
  public String getJokeAndFact() throws ApiException {
    reactor.core.publisher.Mono<String> fact = Rest.webclientServerClient().factsApi().getRandomFact();
    String joke = Rest.feignServerClient().defaultApi().getJokes(new Random().nextInt(3)); //Throwing error? https://github.com/OpenFeign/feign/issues/1042
    String weather = Rest.nativeServerClient().weatherApi().getWeather(new Random().nextInt(3));
    return String.format("Random fact: %s\nRandom joke: %s, Daily weather report: %s", fact, joke, weather);
  }

  @Operation
  @GetMapping("triggerCallToSlowEndpointOnJokesServer")
  public String triggerCallToSlowEndpointOnJokesServer(){
    return Rest.feignServerClient().defaultApi().getSlowResponse(10000);
  }

  @Operation
  @GetMapping("triggerCallToExceptionThrowingEndpointOnJokesServer")
  public String triggerCallToExceptionThrowingEndpointOnJokesServer(){
    try {
      return Rest.feignServerClient().defaultApi().throwException();
    } catch (Throwable t) {
      System.out.println("Client library throw exception: " + t.getClass());
      throw t;
    }
  }

}
