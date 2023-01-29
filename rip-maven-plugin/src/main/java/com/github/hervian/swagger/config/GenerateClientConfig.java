package com.github.hervian.swagger.config;

import com.github.hervian.swagger.generators.ClientGenerator;
import com.github.hervian.swagger.generators.DartClientGenerator;
import com.github.hervian.swagger.generators.JavaClientGenerator;
import com.github.hervian.swagger.installers.ClientInstaller;
import com.github.hervian.swagger.installers.DartClientInstaller;
import com.github.hervian.swagger.installers.JavaMavenClientInstaller;
import com.github.hervian.swagger.publishers.ClientPublisher;
import com.github.hervian.swagger.publishers.DartClientPublisher;
import com.github.hervian.swagger.publishers.JavaMavenClientPublisher;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

@Data
public class GenerateClientConfig {

  public enum Language{
    JAVA(new JavaClientGenerator(), new JavaMavenClientInstaller(), new JavaMavenClientPublisher()),//TODO: Consider adding the library (fx 'feign' or'webclient' as suffix to artifactId such as to allow for generation of multiple java clients distinguishable from a user perspective via the maven tag 'classifier', cf. https://www.baeldung.com/maven-artifact-classifiers
    /**
     * Documentation for the Dart generator: https://openapi-generator.tech/docs/generators/dart/
     */
    DART(new DartClientGenerator(), new DartClientInstaller(), new DartClientPublisher());

    @Getter
    private ClientGenerator clientGenerator;

    @Getter
    private ClientInstaller clientInstaller;

    @Getter
    private ClientPublisher clientPublisher;

    /**
     * When adding a new supported client language one must provide:
     * a ClientGenerator (that creates the source code from the openapi spec)
     * a ClientInstaller (that compiles/installs the source code such as to create a binary that can be used locally)
     * a ClientPublisher that publishes/deploys the compiled artifact to a remote registry, fx Nexus, pub.dev, Artifactory or whatever.
     */
    Language(ClientGenerator clientGenerator, ClientInstaller clientInstaller, ClientPublisher clientPublisher){
      this.clientGenerator = clientGenerator;
      this.clientInstaller = clientInstaller;
      this.clientPublisher = clientPublisher;
    }

  }

  /**
   * Only set this parameter if you are not using the generateDocMojo. The generateDocMojo will generate or download  the json doc and make it available at a specific path.
   * But if you have made it available on a nother opath
   */
  @Parameter
  private String pathToOpenApiDoc;

  @Parameter(defaultValue = "JAVA")
  private List<Language> languages = Lists.newArrayList(Language.JAVA);

  @Parameter
  private JavaConfig javaConfig = new JavaConfig();

  @Parameter
  private DartConfig dartConfig = new DartConfig();

  @Data
  public static class JavaConfig {

    @Parameter(defaultValue = "webclient")
    private String library = "webclient";//feign,webclient,native. See https://openapi-generator.tech/docs/generators/java/.
  }

  @Data
  public static class DartConfig {
    public enum PublishTarget {GIT, DART_REPO;}

    @Parameter
    private String url; //TODO: Use URL type?

    @Parameter(defaultValue = "GIT")
    private PublishTarget publishTarget;

  }

}
