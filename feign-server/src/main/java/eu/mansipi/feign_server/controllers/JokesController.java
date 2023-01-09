package eu.mansipi.feign_server.controllers;

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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Tag(name="jokes", description = "API for joke retrival...")
@Path("/jokes")
public class JokesController {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Operation(responses = @ApiResponse(
      responseCode = "200",
      description = "A joke",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = String.class))))
  @Produces( MediaType.APPLICATION_JSON)
  @GET
  //public Response getJokes(@QueryParam("index") int i){
  public String getJokes(@QueryParam("index") int i){
    // Jersey does not seem smart enough to convert return type string easily into a json object / JSON String
    // Alternatively try to return the string wrapped in escaped quotes as a JSON String.
    String joke;
    switch (i){
      case 0:  joke =  "Hvordan man en fisk til at grine?.\nMan putter den i kildevand.";
      case 1:  joke = "Hvorfor har en stork så lange ben?.\nEllers kunne den ikke nå jorden.";
      default: joke = "Non-existing-joke";
    }
    /*ObjectNode json = mapper.createObjectNode();
    json.put("Joke", "joke");
    return Response.status(Response.Status.OK).entity(json).build();*/
    return "\"" + joke + "\"";
  }

}
