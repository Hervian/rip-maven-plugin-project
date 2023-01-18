package com.github.hervian.swagger.publishers;

import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.config.PropertiesReader;
import com.github.hervian.swagger.generators.ClientGeneratorInput;
import com.github.hervian.swagger.generators.ClientGeneratorOutput;
import com.github.hervian.swagger.installers.ClientInstaller;
import lombok.Builder;
import lombok.Data;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public interface ClientPublisher {

  /**
   * Push the generated client to remote, i.e. publish it.
   * For a Java maven client this means running 'mvn clean deploy'.
   */
  ClientPublisherOutput publish(ClientPublisherInput clientPublisherInput) throws MojoExecutionException;

  @Builder
  @Data
  class ClientPublisherInput {
    private Log log;
    private MavenProject project;
    private MavenSession mavenSession;
    private final BuildPluginManager pluginManager;
    private final PropertiesReader propertiesReader;
    private GenerateClientConfig clientConfig;
    private ClientGeneratorOutput clientGeneratorOutput;
  }

  class ClientPublisherOutput {
    String path;
  }

}
