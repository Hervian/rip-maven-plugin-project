package eu.mansipi.feign_server.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Date;

@Component
@Tag(name="time", description = "API for time retrival...")
@Path("/time")
public class TimeController {

  @Operation
  @GET
  public String getNow(){
    return new Date().toString();
  }

}
