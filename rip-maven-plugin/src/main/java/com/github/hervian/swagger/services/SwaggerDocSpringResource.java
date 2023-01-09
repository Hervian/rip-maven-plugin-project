package com.github.hervian.swagger.services;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("")
public class SwaggerDocSpringResource {

  @Value("${server.port}")
  private int serverPort;

  /*@Context //What is the equivalent of @Context UriInfo in Spring Rest: https://stackoverflow.com/q/34690109/6095334
  UriInfo uriInfo;*/

  /*@Operation(hidden = true)
  @GetMapping(value = "/swagger/swagger.json", produces = MediaType.APPLICATION_JSON_VALUE)
  public InputStream getSwaggerJson(){ //TODO fix: error 500 No Encoder for [java.io.InputStream] with preset Content-Type 'null'
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream resource = classloader.getResourceAsStream("swagger/swagger.json");
    System.out.println("swagger.json resource!=null: "+Boolean.valueOf(resource!=null));
    return resource;
  }*/

  @Operation(hidden = true)
  //@ResponseBody
  @GetMapping(value = "/openapi")
  public ResponseEntity<byte[]> getOpenApiJson() throws IOException {
    return getSwaggerJson();
  }

  @Operation(hidden = true)
  //@ResponseBody
  @GetMapping(value = "/swagger.json", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<byte[]> getSwaggerJson() throws IOException {
    ClassLoader var1 = Thread.currentThread().getContextClassLoader();
    try (InputStream var2 = var1.getResourceAsStream("swagger/swagger.json")){
      String swaggerJson = StreamUtils.copyToString(var2, Charset.forName("UTF-8"));
      swaggerJson = swaggerJson.replace("localhost:8080", "localhost:"+serverPort);
      return ResponseEntity.ok().body(swaggerJson.getBytes(StandardCharsets.UTF_8));
    }
  }
  /*public Object getSwaggerJson(){
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream resource = classloader.getResourceAsStream("swagger/swagger.json");
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(resource, Object.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }*/

  //TODO: make below work
  @Operation(hidden = true)
  @GetMapping(value = "/swagger/swagger.html", produces = "text/html")
  public Mono<Void> getSwaggerHtml(ServerHttpResponse response, ServerHttpRequest request) throws IOException { //https://www.knowledgefactory.net/2021/09/spring-webflux-file-download-rest-api.html
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    //spring webmvn example of getting the path from the getMapping: https://stackoverflow.com/a/37718400/6095334
    String path = request.getURI().getPath(); //https://stackoverflow.com/a/71208502/6095334
    InputStream inputStream = classloader.getResourceAsStream(path);
    File swaggerHtmlFile = File.createTempFile("swagger", "html");
    swaggerHtmlFile.deleteOnExit();
    copyInputStreamToFile(inputStream, swaggerHtmlFile);

    ContentDisposition contentDisposition = ContentDisposition.attachment()
      .filename("swagger.html")
      .build();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(contentDisposition);

    ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
    response.getHeaders().setContentDisposition(contentDisposition);
    response.getHeaders().setContentType(MediaType.
      APPLICATION_OCTET_STREAM);

    return zeroCopyResponse.writeWith(swaggerHtmlFile, 0, swaggerHtmlFile.length());
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
