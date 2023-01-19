package eu.mansipi.webclient_server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/*@RestController
@RequestMapping({"/doc2"})*/
@Deprecated
public class SwaggerDocSpringResource2 {

  /*@Value("${server.port}")
  private int serverPort;

  @Operation(
      hidden = true
  )
  @GetMapping(
      value = {"/swagger/swagger.json"},
      produces = {"application/json"}
  )
  public ResponseEntity<byte[]> getSwaggerJson() throws IOException {
    ClassLoader var1 = Thread.currentThread().getContextClassLoader();
    InputStream var2 = var1.getResourceAsStream("swagger/swagger.json");
    String swaggerJson = StreamUtils.copyToString(var2, Charset.forName("UTF-8"));
    swaggerJson = swaggerJson.replace("localhost:8080", "localhost:"+serverPort);
    return ResponseEntity.ok().body(swaggerJson.getBytes(StandardCharsets.UTF_8));
  }

  @Operation(
      hidden = true
  )
  @GetMapping(
      value = {"/swagger/swagger.html"},
      produces = {"text/html"}
  )
  public Mono<Void> getSwaggerHtml(ServerHttpResponse var1, ServerHttpRequest var2) throws IOException {
    ClassLoader var3 = Thread.currentThread().getContextClassLoader();
    String var4 = var2.getURI().getPath();
    System.out.println("path = request.getURI().getPath() = " + var4);
    InputStream var5 = var3.getResourceAsStream(var4);
    File var6 = File.createTempFile("swagger", "html");
    var6.deleteOnExit();
    copyInputStreamToFile(var5, var6);
    ContentDisposition var7 = ContentDisposition.attachment().filename("swagger.html").build();
    HttpHeaders var8 = new HttpHeaders();
    var8.setContentDisposition(var7);
    ZeroCopyHttpOutputMessage var9 = (ZeroCopyHttpOutputMessage)var1;
    var1.getHeaders().setContentDisposition(var7);
    var1.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
    return var9.writeWith(var6, 0L, var6.length());
  }

  private static void copyInputStreamToFile(InputStream var0, File var1) throws IOException {
    FileOutputStream var2 = new FileOutputStream(var1, false);

    try {
      byte[] var4 = new byte[8192];

      int var3;
      while((var3 = var0.read(var4)) != -1) {
        var2.write(var4, 0, var3);
      }
    } catch (Throwable var6) {
      try {
        var2.close();
      } catch (Throwable var5) {
        var6.addSuppressed(var5);
      }

      throw var6;
    }

    var2.close();
  }*/
}

