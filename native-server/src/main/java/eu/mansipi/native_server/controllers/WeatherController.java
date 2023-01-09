package eu.mansipi.native_server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Tag(name="weather", description = "API for weather retrival...")
@Path("/weather")
public class WeatherController {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Operation(responses = @ApiResponse(
      responseCode = "200",
      description = "Guess the weather",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = String.class))))
  @Produces( MediaType.APPLICATION_JSON)
  @GET
  //public Response getJokes(@QueryParam("index") int i){
  public String getWeather(@QueryParam("index") int i){
    // Jersey does not seem smart enough to convert return type string easily into a json object / JSON String
    // Alternatively try to return the string wrapped in escaped quotes as a JSON String.
    String weather;
    switch (i){
      case 0:  weather =  "It is raining";
      case 1:  weather = "It is sunny.";
      default: weather = "It is snowing";
    }
    return "\"" + weather + "\"";
  }

}
