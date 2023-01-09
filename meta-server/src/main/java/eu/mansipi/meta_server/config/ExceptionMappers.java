package eu.mansipi.meta_server.config;


import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

public class ExceptionMappers {

  @Component
  @Provider //Not working. Why? https://www.baeldung.com/java-exception-handling-jersey
  public static class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    public Response toResponse(Throwable ex) {
      ex.printStackTrace();
      return Response.status(Response.Status.EXPECTATION_FAILED)
          .entity(RestErrorResponse
              .builder()
              .clazz(ex.getClass())
              .message(ex.getMessage())
              .build())
          .type(MediaType.APPLICATION_JSON)
          .build();
    }


    @Builder
    @Data
    public static class RestErrorResponse { //TODO: This ought to be some RFC7807 ProblemDetails object.
      private Class clazz;
      private String message;
    }
  }

}
