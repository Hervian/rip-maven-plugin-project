package com.github.hervian.swagger.mojos;

import com.github.hervian.swagger.config.PropertiesReader;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

public interface PluginExecutionInfoProvider {

  default Plugin getPlugin(MavenProject project, PropertiesReader propertiesReader) {
    //System.out.println("mavenSession.getGoals(): " +mavenSession.getGoals()); //returns stuff like 'clean, verify'

    Plugin swaggerPlugin = project.getPlugin(String.format("%s:%s", propertiesReader.getGroupId(), propertiesReader.getArtifactId()));
    return swaggerPlugin;
    /*for (PluginExecution pluginExecution: swaggerPlugin.getExecutions()){ //Consider using maven-replacer-plugin https://gist.github.com/4n3w/3365657
      listOfGoals = pluginExecution.getGoals();
      System.out.println("goals: " + listOfGoals);
      System.out.println("phases: " + pluginExecution.getPhase());
    }*/
  }



}
