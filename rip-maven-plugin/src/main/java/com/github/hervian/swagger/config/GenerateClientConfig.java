package com.github.hervian.swagger.config;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

@Data
public class GenerateClientConfig {

  public enum Language{
    JAVA,//TODO: Consider adding the library (fx 'feign' or'webclient' as suffix to artifactId such as to allow for generation of multiple java clients distinguishable from a user perspective via the maven tag 'classifier', cf. https://www.baeldung.com/maven-artifact-classifiers
    DART;
  }

  @Parameter(defaultValue = "JAVA")
  private List<Language> languages = Lists.newArrayList(Language.JAVA);

  @Parameter
  private JavaConfig javaConfig = new JavaConfig();

  @Data
  public static class JavaConfig {

    @Parameter(defaultValue = "webclient")
    private String library = "webclient";//feign,webclient,native. See https://openapi-generator.tech/docs/generators/java/.
  }

}
