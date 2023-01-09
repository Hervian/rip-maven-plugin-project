package eu.mansipi.meta_server.controllers;

import eu.mansipi.meta_server.controllers.rest.Rest;
import eu.mansipi.native_server.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Random;

@Component
@Tag(name="meta", description = "API for calling other servers...")
@Path("/meta")
public class JokesAndRandomFactsController {

  @Operation
  @GET @Path(("callAllServers"))
  public String getJokeAndFact() throws ApiException {
    reactor.core.publisher.Mono<String> fact = Rest.webclientServerClient().factsApi().getRandomFact();
    String joke = Rest.feignServerClient().jokesApi().getJokes(new Random().nextInt(3)); //Throwing error? https://github.com/OpenFeign/feign/issues/1042
    String weather = Rest.nativeServerClient().weatherApi().getWeather(new Random().nextInt(3));
    return String.format("Random fact: %s\nRandom joke: %s, Daily weather report: %s", fact, joke, weather);
  }

  @Operation
  @GET @Path("triggerCallToSlowEndpointOnJokesServer")
  public String triggerCallToSlowEndpointOnJokesServer(){
    return Rest.feignServerClient().sickServerApi().getSlowResponse(10000);
  }

  @Operation
  @GET @Path("triggerCallToExceptionThrowingEndpointOnJokesServer")
  public String triggerCallToExceptionThrowingEndpointOnJokesServer(){
    try {
      return Rest.feignServerClient().sickServerApi().throwException();
    } catch (Throwable t) {
      System.out.println("Client library throw exception: " + t.getClass());
      throw t;
    }
  }

}
