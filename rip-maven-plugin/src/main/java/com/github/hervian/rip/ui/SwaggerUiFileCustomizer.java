package com.github.hervian.rip.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.BiFunction;

//TODO: Instead of this class / the BiFunction pattern, simply add a static nested class to SwaggerUiJaxRsResource that people must extend
public class SwaggerUiFileCustomizer implements BiFunction<String, String, String>  {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUiFileCustomizer.class);

  @Override
  public String apply(String fileName, String content) {
    if (fileName.endsWith("index.html")){
      LOGGER.info("Customizing 'index.html'");
      content = updateIndexHtml(content);
    }
    return content;
  }

  private String updateIndexHtml(String content) {
    //TODO: add download html link + create resource/extend existing resource
    String downloadHtmlSnippet = getDownloadHtmlJavascriptSnippet();

    content = replaceLastOccurenceOf(content, "</script>", downloadHtmlSnippet);//content.replaceAll("</script>$",getDownloadHtmlJavascriptSnippet() + "</script>");
    return content;
  }

  private String getDownloadHtmlJavascriptSnippet() {
    InputStream in = getClass().getResourceAsStream("/downloadHtmlSnippet.js");
    String jsSnippetThatAddsDownloadHtmlLink = null;
    try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name())) {
      jsSnippetThatAddsDownloadHtmlLink = scanner.useDelimiter("\\A").next();
    }
    return jsSnippetThatAddsDownloadHtmlLink;
  }

  private String replaceLastOccurenceOf(String str, String strToReplace, String replacement){
    int index = str.lastIndexOf(strToReplace);
    StringBuilder builder = new StringBuilder();
    builder.append(str, 0, index);
    builder.append(replacement);
    builder.append(str.substring(index));
    return builder.toString();
  }

}
