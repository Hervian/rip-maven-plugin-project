package com.github.hervian.rip.ui.src;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/openapi")
public class SwaggerUiSpringResource {

  @EventListener({ApplicationReadyEvent.class})
  public void logPathToSwaggerUi() {
    System.out.println("SwaggerUI is available at .../openapi/swagger-ui.html");
  }

  @Operation(hidden = true)
  @GetMapping(value="", produces = MediaType.TEXT_HTML_VALUE)
  public byte[] getSwaggerUiOnBasPath() throws IOException {
    return getSwaggerUiHtml();
  }

  @Operation(hidden = true)
  @GetMapping(value="/swagger-ui", produces = MediaType.TEXT_HTML_VALUE)
  public byte[] getSwaggerUi() throws IOException {
    return getSwaggerUiHtml();
  }

  @Operation(hidden = true)
  @GetMapping(value="/swagger-ui.html", produces = MediaType.TEXT_HTML_VALUE)
  public @ResponseBody byte[] getSwaggerUiHtml() throws IOException {
    /*System.out.println(uriInfo.getPath());
    for (PathSegment pathSegment: uriInfo.getPathSegments()){
      System.out.println(pathSegment.getPath());
    }
    String fileName = uriInfo.getPath().substring(uriInfo.getPath().lastIndexOf("/"));
    System.out.println("fileName = " + fileName);*/
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    InputStream resource4 = classloader.getResourceAsStream(String.format("swagger/ui/index.html"));
    return StreamUtils.copyToByteArray(resource4);
  }

 /* @Operation(hidden = true)
  //@GET @Path("swagger/ui/{var:.+}") //https://www.logicbig.com/how-to/code-snippets/jcode-jax-rs-path-param-regex-match-all.html
  @GetMapping(value="/swagger/ui/{wildcard:.*}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE) //https://www.baeldung.com/spring-5-mvc-url-matching
  public @ResponseBody byte[] getSwaggerUiFiles(@PathVariable("wildcard") String wildcard) throws IOException {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    System.out.println("@PathVariable String path = " + wildcard);
    InputStream resource4 = classloader.getResourceAsStream("swagger/ui/" + wildcard);
    System.out.println("resource4!=null: "+Boolean.valueOf(resource4!=null));
    return StreamUtils.copyToByteArray(resource4);
  }*/


  @Operation(
    hidden = true
  )
  @GetMapping(value="/swagger/ui/{wildcard:.*}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> getSwaggerUiFiles(@PathVariable("wildcard") String wildcard) throws IOException {
    ClassLoader var2 = Thread.currentThread().getContextClassLoader();
    InputStream var3 = getResourceAsStream(wildcard, var2);
    byte[] byteArray = StreamUtils.copyToByteArray(var3);
    String contentType = null;
    String fileType = wildcard.substring(wildcard.lastIndexOf(".")+1);
    switch (fileType){
      case "js": contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; break;
      case "png": contentType = MediaType.IMAGE_PNG_VALUE; break;
      case "css": contentType = "text/css"; break;
    }
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("Content-Type", contentType);
    return ResponseEntity.ok()
      .headers(responseHeaders)
      .body(byteArray);
  }

  private InputStream getResourceAsStream(@PathVariable String wildcard, ClassLoader var2) {
    return var2.getResourceAsStream("swagger/ui/"+wildcard);
  }

/*  @Operation(
    hidden = true
  )
  @GetMapping(value="/swagger/ui/{wildcard:.*map}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody byte[] getSwaggerUiMapFiles(@PathVariable String wildcard) throws IOException {
    ClassLoader var2 = Thread.currentThread().getContextClassLoader();
    System.out.println("@PathVariable String path = " + wildcard);
    InputStream var3 = getResourceAsStream(wildcard, var2);
    return StreamUtils.copyToByteArray(var3);
  }

  @Operation(
    hidden = true
  )
  @GetMapping(value="/swagger/ui/{wildcard:.*png}", produces = MediaType.IMAGE_PNG_VALUE)
  public @ResponseBody byte[] getSwaggerUiPngFiles(@PathVariable String wildcard) throws IOException {
    ClassLoader var2 = Thread.currentThread().getContextClassLoader();
    System.out.println("@PathVariable String path = " + wildcard);
    InputStream var3 = getResourceAsStream(wildcard, var2);
    return StreamUtils.copyToByteArray(var3);
  }

  @Operation(
    hidden = true
  )
  @GetMapping(value="/swagger/ui/{wildcard:.*css}", produces = "text/css")
  public @ResponseBody byte[] getSwaggerUiCssFiles(@PathVariable String wildcard) throws IOException {
    ClassLoader var2 = Thread.currentThread().getContextClassLoader();
    System.out.println("@PathVariable String path = " + wildcard);
    InputStream var3 = getResourceAsStream(wildcard, var2);
    return StreamUtils.copyToByteArray(var3);
  }

  @Operation(
    hidden = true
  )
  @GetMapping(value="/swagger/ui/{wildcard:.*js}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)//"application/javascript; charset=utf-8")//
  public @ResponseBody byte[] getSwaggerUiJsFiles(@PathVariable String wildcard) throws IOException {
    ClassLoader var2 = Thread.currentThread().getContextClassLoader();
    System.out.println("@PathVariable String path = " + wildcard);
    InputStream var3 = getResourceAsStream(wildcard, var2);
    return StreamUtils.copyToByteArray(var3);
  }*/
}
