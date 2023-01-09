package eu.mansipi.feign_server.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Tag(name="sick-server", description = "API for testing timeouts etc...")
@Path("/sick-server")
public class SickServerApi {

  @Operation()
  @Produces( MediaType.APPLICATION_JSON)
  @GET @Path("/sleep")
  public String getSlowResponse(@QueryParam("ms") int i) throws InterruptedException {
    Thread.sleep(i);
    String str = "Slept " + i + "ms";
    System.out.println(str);
    return "\"" + str + "\"";
  }

  @Operation()
  @Produces( MediaType.APPLICATION_JSON)
  @GET @Path("/throwException")
  public String throwException() {
    if (true)
    throw new RuntimeException("throwing exception to mimic that something went wrong.");
    return "\"" + "I don't think this value is returned" + "\"";
  }
}
