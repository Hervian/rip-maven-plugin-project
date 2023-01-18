package com.github.hervian.swagger.installers;

import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.config.PropertiesReader;
import com.github.hervian.swagger.generators.ClientGeneratorOutput;
import lombok.Builder;
import lombok.Data;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public interface ClientInstaller {

  /**
   * When a REST client has been created via the openapi generator we must install it
   * such as to make it available locally for development/usage.
   * For generated java clients this means running 'mvn clean install'.
   * Other target languages may have similar concepts.
   */
  ClientInstallerOutput install(ClientInstallerInput clientInstallerInput) throws MojoExecutionException;

  @Builder
  @Data
  class ClientInstallerInput {
    private Log log;
    private MavenProject project;
    private MavenSession mavenSession;
    private final BuildPluginManager pluginManager;
    private final PropertiesReader propertiesReader;
    private GenerateClientConfig clientConfig;
    private ClientGeneratorOutput clientGeneratorOutput;
  }

  @Builder
  @Data
  class ClientInstallerOutput {
    String path;
  }

}
