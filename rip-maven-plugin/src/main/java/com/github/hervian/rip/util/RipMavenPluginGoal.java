package com.github.hervian.rip.util;

import com.github.hervian.rip.config.PropertiesReader;
import lombok.Getter;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.util.List;

public enum RipMavenPluginGoal {
    GENERATE_REST("generateRest"),
    GENERATE_DOC("generateDoc"),
    DIFF("diff"),
    GENERATE_UI("generateUi"),
    GENERATE_CLIENT("generateClient"),
    RIP("rip");

    @Getter
    private String goal;

    private RipMavenPluginGoal(String goal) {
      this.goal = goal;
    }

    public boolean isActivated(MavenProject project, PropertiesReader propertiesReader) throws MojoExecutionException {
      List<String> listOfGoals = getListOfGoals(project, propertiesReader);

      if (listOfGoals!=null) {
        for (String activatedGoal : listOfGoals) {
          if (this.getGoal().equalsIgnoreCase(activatedGoal)) {
            return true;
          }
        }
      }
      return false;
    }

  public static List<String> getListOfGoals(MavenProject project, PropertiesReader propertiesReader) throws MojoExecutionException {
    Plugin swaggerPlugin = project.getPlugin(String.format("%s:%s", propertiesReader.getGroupId(), propertiesReader.getArtifactId()));
    List<PluginExecution> pluginExecutions = swaggerPlugin.getExecutions();
    if (pluginExecutions.isEmpty()) {
      throw new MojoExecutionException("Unexpected situation: the rip-maven-plugin is called but no goals can be found...");
    }
    if (pluginExecutions.size()>1) {
      throw new MojoExecutionException("The rip-maven-plugin does not yet support multiple execution blocks.");
    }
    PluginExecution pluginExecution = swaggerPlugin.getExecutions().get(0); //Consider using maven-replacer-plugin https://gist.github.com/4n3w/3365657
    List<String> listOfGoals = pluginExecution.getGoals();
    return listOfGoals;
  }

}
