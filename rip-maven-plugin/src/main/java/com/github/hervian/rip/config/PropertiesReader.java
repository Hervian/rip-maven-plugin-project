package com.github.hervian.rip.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Based on https://www.baeldung.com/java-accessing-maven-properties
 */
public class PropertiesReader {
  private Properties properties;

  public PropertiesReader() throws IOException {
    String propertyFileName = "properties-from-pom.properties";
    InputStream is = getClass().getClassLoader()
        .getResourceAsStream(propertyFileName);
    this.properties = new Properties();
    this.properties.load(is);
  }

  public String getProperty(String propertyName) {
    return this.properties.getProperty(propertyName);
  }

  public String getArtifactId(){
    return getProperty("artifactId");
  }

  public String getGroupId(){
    return getProperty("groupId");
  }

  public boolean isSkipGenerateDoc() {
    return Boolean.getBoolean(getProperty("skipGenerateDoc"));
  }

  public boolean isSkipGenerateUi() {
    return Boolean.getBoolean(getProperty("skipGenerateUi"));
  }

  public String getReleaseRepositoryUrl(){
    return getProperty("project.distributionManagement.repository.url");
  }

  public String getSwaggerBrakeVersion(){
    return getProperty("swagger-brake.version");
  }

  public String getOrgOpenapitoolsVersion() {
    return getProperty("org.openapitools.version");
  }

  public String getExecMavenPluginVersion() {
    return getProperty("exec.maven.plugin");
  }

  public String getSpringDocOpenApiMavenPluginVersion() {
    return getProperty("springdoc.openapi.maven.plugin.version");
  }

  public String getSpringBootVersion() {
    return getProperty("spring.boot.version");
  }

  public String getBuildHelperMavenPluginVersion() {
    return getProperty("build.helper.maven.plugin");
  }
}
