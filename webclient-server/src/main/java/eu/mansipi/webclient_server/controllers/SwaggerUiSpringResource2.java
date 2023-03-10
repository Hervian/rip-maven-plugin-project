package eu.mansipi.webclient_server.controllers;

import io.swagger.v3.oas.annotations.Operation;
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

/*@RestController
@RequestMapping({"/doc2"})*/
@Deprecated
public class SwaggerUiSpringResource2 {
  /*@Operation(
      hidden = true
  )
  @GetMapping(value="/swagger-ui", produces = MediaType.TEXT_HTML_VALUE)
  public @ResponseBody byte[] getSwaggerUi() throws IOException {
    return this.getSwaggerUiHtml();
  }

  @Operation(
      hidden = true
  )
  @GetMapping(value="/swagger-ui.html", produces = MediaType.TEXT_HTML_VALUE)
  public @ResponseBody byte[] getSwaggerUiHtml() throws IOException {
    ClassLoader var1 = Thread.currentThread().getContextClassLoader();
    InputStream var2 = var1.getResourceAsStream("swagger/ui/index.html");
    return StreamUtils.copyToByteArray(var2);
  }

  *//*@Operation(
      hidden = true
  )
  @GetMapping(value="/swagger/ui/{wildcard:.*html.*}", produces = MediaType.TEXT_HTML_VALUE)
  public @ResponseBody byte[] getSwaggerUiHtmlFiles(@PathVariable String wildcard) throws IOException {
    ClassLoader var2 = Thread.currentThread().getContextClassLoader();
    System.out.println("@PathVariable String path = " + wildcard);
    InputStream var3 = var2.getResourceAsStream(wildcard);
    return StreamUtils.copyToByteArray(var3);
  }*//*

  *//*
  Below works except for the
  @Operation(
      hidden = true
  )
  @GetMapping(value="/swagger/ui/{wildcard:.*}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody byte[] getSwaggerUiFiles(@PathVariable("wildcard") String wildcard) throws IOException {
    ClassLoader var2 = Thread.currentThread().getContextClassLoader();
    System.out.println("@PathVariable String path = " + wildcard);
    InputStream var3 = getResourceAsStream(wildcard, var2);
    byte[] byteArray = StreamUtils.copyToByteArray(var3);
    return byteArray:
  }*//*

  @Operation(
      hidden = true
  )
  @GetMapping(value="/swagger/ui/{wildcard:.*}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> getSwaggerUiFiles(@PathVariable("wildcard") String wildcard) throws IOException {
    ClassLoader var2 = Thread.currentThread().getContextClassLoader();
    System.out.println("@PathVariable String path = " + wildcard);
    InputStream var3 = getResourceAsStream(wildcard, var2);
    byte[] byteArray = StreamUtils.copyToByteArray(var3);
    String contentType = null;
    String fileType = wildcard.substring(wildcard.lastIndexOf(".")+1);
    System.out.println("FileType = " + fileType);
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
*//*
  @Operation(
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
  }*//*
*/
}
