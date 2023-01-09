package com.github.hervian.swagger.generators.docs;

import com.github.hervian.swagger.config.GenerateDocConfig;
import lombok.Builder;
import lombok.Data;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

@Data
@Builder
public class DocumentGeneratorInput {

  private final MavenProject project;
  private final MavenSession mavenSession;
  private final BuildPluginManager pluginManager;
  private final Log log;

  private final GenerateDocConfig generateDocConfig;
  private final String outputDir;

}
