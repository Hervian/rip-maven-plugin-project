package com.github.hervian.swagger.services;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/openapi")
public class SwaggerDocSpringResource {

  @Value("${server.port}")
  private int serverPort;

  @EventListener({ApplicationReadyEvent.class})
  public void logPathToSwaggerUi() {
    System.out.println("Swagger.json is available at ...${apiDocsUrl}");
    System.out.println("Swagger.html is available at .../openapi/swagger.html");
  }

  /*@Context //What is the equivalent of @Context UriInfo in Spring Rest: https://stackoverflow.com/q/34690109/6095334
  UriInfo uriInfo;*/

      @Operation(hidden = true)
  @GetMapping(value = "/swagger/swagger.json", produces = MediaType.APPLICATION_JSON_VALUE)
  public InputStream getSwaggerJson(){ //TODO fix: error 500 No Encoder for [java.io.InputStream] with preset Content-Type 'null'
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream resource = classloader.getResourceAsStream("${swagger.json.path}");
    System.out.println("swagger.json resource!=null: "+Boolean.valueOf(resource!=null));
    return resource;
  }


  @Operation(hidden = true)
  @GetMapping(value = "/swagger.html", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public FileSystemResource getSwaggerHtml() throws IOException { //https://www.knowledgefactory.net/2021/09/spring-webflux-file-download-rest-api.html
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classloader.getResourceAsStream("swagger/swagger.html");
    File swaggerHtmlFile = File.createTempFile("swagger", "html");
    swaggerHtmlFile.deleteOnExit();
    copyInputStreamToFile(inputStream, swaggerHtmlFile);
    return new FileSystemResource(swaggerHtmlFile);
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
