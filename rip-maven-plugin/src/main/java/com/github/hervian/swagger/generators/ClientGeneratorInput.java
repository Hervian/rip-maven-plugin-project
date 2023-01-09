package com.github.hervian.swagger.generators;

import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.config.PropertiesReader;
import lombok.Builder;
import lombok.Data;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

@Builder
@Data
public class ClientGeneratorInput {

  private final String pathToSwaggerDoc;
  private final PropertiesReader propertiesReader;
  private final MavenProject project;
  private final MavenSession mavenSession;
  private final BuildPluginManager pluginManager;
  private final Log log;
  private GenerateClientConfig generateClientConfig;

}
