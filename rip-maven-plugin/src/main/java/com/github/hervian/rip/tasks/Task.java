package com.github.hervian.rip.tasks;

import com.github.hervian.rip.tasks.config.GenerateRestConfig;
import com.github.hervian.swagger.config.GenerateClientConfig;
import com.github.hervian.swagger.config.GenerateDocConfig;
import com.github.hervian.swagger.config.GenerateUiConfig;
import com.github.hervian.swagger.config.PropertiesReader;
import lombok.Builder;
import lombok.Data;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.project.MavenProject;

import java.util.List;

public interface Task {

  void execute(TaskInput taskInput) throws MojoExecutionException;

  @Builder
  @Data
  class TaskInput {
    private PropertiesReader propertiesReader;
    private LifecyclePhase lifecyclePhase;
    private List<TaskInput> taskInputs;

    private MavenProject project;
    private MavenSession mavenSession;
    private BuildPluginManager pluginManager;
    private Log log;

    private GenerateDocConfig generateDocConfig;
    private GenerateUiConfig generateUiConfig;
    private GenerateClientConfig generateClientConfig;
    //private GenerateRestConfig wrapClientsConfig;
  }

}
