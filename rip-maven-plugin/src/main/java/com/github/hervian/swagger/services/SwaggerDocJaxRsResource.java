package com.github.hervian.swagger.services;


import io.swagger.v3.oas.annotations.Operation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StreamUtils;

@ConditionalOnProperty(value = "swagger.ui.disabled", havingValue = "false", matchIfMissing = true) //Give Spring users an option to exclude swagger-ui from, say deployments to prod environment by setting swagger.ui.disabled=true in application-prod
@Component
@Path("/doc") //TODO: Insert configurable base path? If done, remember to update GenerateSwaggerDocMojo which has a hard coded reference to 'doc' when specifying the url of the swagger.json in the index.html
public class SwaggerDocJaxRsResource  {

  @Value("${server.port}")
  private int serverPort;

  @Context //What is the equivalent of @Context UriInfo in Spring Rest: https://stackoverflow.com/q/34690109/6095334
  UriInfo uriInfo;

  //TODO: parse inputStream/file into string and make a search-and-replace on the port number of the server such as to insert the actual port of the running app. See com.github.hervian.swagger.services.SwaggerDocSpringResource
  @Operation(hidden = true)
  @GET @Path("swagger/swagger.json") @Produces("application/json")
  public Response getSwaggerJson() throws IOException {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream resource = classloader.getResourceAsStream("swagger/swagger.json");
    /*System.out.println("swagger.json resource!=null: "+Boolean.valueOf(resource!=null));
    return resource;*/
    String swaggerJson = StreamUtils.copyToString(resource, Charset.forName("UTF-8"));
    swaggerJson = swaggerJson.replace("localhost:8080", "http://localhost:"+serverPort + "/");
    return Response.ok(swaggerJson.getBytes(StandardCharsets.UTF_8)).build();
  }

  @Operation(hidden = true)
  @GET @Path("swagger/swagger.html") @Produces("text/html")
  public Response getSwaggerHtml() throws IOException {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    String path = uriInfo.getPath().substring(uriInfo.getPath().indexOf("/")+1);
    System.out.println("uriInfo.getPath() = " + uriInfo.getPath());
    System.out.println("uriInfo.getPath().substring(uriInfo.getPath().indexOf(\"/\")+1) = " + path);
    InputStream inputStream = classloader.getResourceAsStream(path);
    File swaggerHtmlFile = File.createTempFile("swagger", "html");
    swaggerHtmlFile.deleteOnExit();
    //FileUtils.copyInputStreamToFile(inputStream, swaggerHtmlFile);
    copyInputStreamToFile(inputStream, swaggerHtmlFile);

    Response.ResponseBuilder response = Response.ok((Object) swaggerHtmlFile);
    response.header("Content-Disposition","attachment; filename=swagger.html");
    return response.build();
  }

  private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
    try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
      int read;
      byte[] bytes = new byte[8192];
      while ((read = inputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }
    }
  }


}
