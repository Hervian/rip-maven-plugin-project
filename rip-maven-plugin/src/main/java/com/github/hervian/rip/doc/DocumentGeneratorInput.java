package com.github.hervian.rip.doc;

import com.github.hervian.rip.config.GenerateDocConfig;
import com.github.hervian.rip.config.PropertiesReader;
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
  private final PropertiesReader propertiesReader;

  private final GenerateDocConfig generateDocConfig;
  private final String outputDir;

}
