package com.github.hervian.rip.config;

import lombok.Builder;
import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

@Data
public class GenerateUiConfig {

  /**
   * A fully qualified class name of a java.lang.BiFunction<String, String, String>.
   * The customizer gives the user of this plugin the possibility to customize the various swagger-ui files (js, css and html).
   * Sometimes this is convenient, fx, sometimes one wants to style the swagger-ui app differently for each environment
   * it is deployed to (like green for test env, red for prod).
   * Please note that the first input parameter to your overriden apply(String, String) method will be the name of the file, fx 'index.html'
   * and the second will be the whole file content.
   * The return value should be a modified version of the file content.
   */
  @Parameter()
  private String fileCustomizer;

  /**
   * Only set this parameter if you are not using the generateDocMojo.
   * The generateDocMojo will generate or download  the json doc and make it available at a specific path.
   * If you are using springdoc to generate the openapi docs it will by standard be available at /v3/api-docs
   */
  @Parameter(defaultValue = "/v3/api-docs")
  private String pathToOpenApiDoc = "/v3/api-docs";

  /*@Parameter
  private boolean skipGenerateUi*/

}
