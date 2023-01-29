package com.github.hervian.swagger.generators.docs;

import com.github.hervian.swagger.util.MojoExecutorWrapper;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * This generator is simply a runtime implementation of the following plugin flow (as described here: https://stackoverflow.com/a/61210466/6095334 and here: https://github.com/springdoc/springdoc-openapi-maven-plugin):
 *
 * <plugin>
 *         <groupId>org.springframework.boot</groupId>
 *         <artifactId>spring-boot-maven-plugin</artifactId>
 *         <!--<configuration>
 *           &lt;!&ndash;<directories>C:\Users\m83522\.m2\repository\org\springframework\boot\spring-boot-starter-webflux\2.7.0</directories>&ndash;&gt;
 *         </configuration>-->
 *         <executions>
 *           <execution>
 *             <id>pre-integration-test</id>
 *             <goals>
 *               <goal>start</goal>
 *             </goals>
 *           </execution>
 *           <execution>
 *             <id>post-integration-test</id>
 *             <goals>
 *               <goal>stop</goal>
 *             </goals>
 *           </execution>
 *         </executions>
 *       </plugin>
 *       <plugin>
 *         <groupId>org.springdoc</groupId>
 *         <artifactId>springdoc-openapi-maven-plugin</artifactId>
 *         <version>1.4</version>
 *         <executions>
 *           <execution>
 *             <phase>integration-test</phase>
 *             <goals>
 *               <goal>generate</goal>
 *             </goals>
 *           </execution>
 *         </executions>
 *         <configuration>
 *           <outputFileName>openapi.json</outputFileName>
 *           <outputDir>${project.build.directory}/openapi-spec</outputDir>
 *           <apiDocsUrl>http://localhost:8080/v3/api-docs</apiDocsUrl>
 *         </configuration>
 *       </plugin>
 */
public class RestEndpointCallingOpenApiDocumentGenerator implements OpenApiDocumentGenerator{

  /**
   * Uses https://github.com/springdoc/springdoc-openapi to generate a swagger.json doc by
   * scanning code for Spring Webflux REST endpoints.
   * @param documentGeneratorInput
   */
  @Override
  public void generate(DocumentGeneratorInput documentGeneratorInput) throws MojoExecutionException {
    /**
     * java -cp 'myMproject.jar;spring-boot-starter-webflux.jar' myMainClass
     * Call spring-boot-maven-plugin start (Or start and stop app manually, via PiD: https://stackoverflow.com/a/34875139/6095334
     *    add JVM option: -jar to add the jar spring-boot-starter-webflux
     * Call http://localhost:8080/v3/api-docs and save json to file. See fx springdoc-openapi-maven-plugin: https://github.com/springdoc/springdoc-openapi-maven-plugin/blob/14273ef2fb2fe2d1e9c0818c983f1c02f717fb87/src/main/java/org/springdoc/maven/plugin/SpringDocMojo.java#L112
     * Call spring-boot-maven-plugin stop. Best to use the Pid. One cannot expect actuator/shutdown to be enabled (http://localhost:8080/actuator/shutdown).
     */
    reserveRandomPort(documentGeneratorInput);
    try {
      startServer(documentGeneratorInput);
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException(e);
    }
    getOpenApiDoc(documentGeneratorInput);
    stopServer(documentGeneratorInput);

  }

  private void reserveRandomPort(DocumentGeneratorInput input) throws MojoExecutionException {
    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("org.codehaus.mojo"),
        artifactId("build-helper-maven-plugin"),//https://github.com/openapi-tools/swagger-maven-plugin
        version(input.getPropertiesReader().getBuildHelperMavenPluginVersion())
      ),
      goal("reserve-network-port"),
      configuration(
        //WiP:
        element(name("portNames"),
          element(name("portName"), "tomcat.http.port")
        )
      ),
      executionEnvironment(
        input.getProject(),
        input.getMavenSession(),
        input.getPluginManager()
      ));
  }

  private void startServer(DocumentGeneratorInput input) throws MojoExecutionException, DependencyResolutionRequiredException {
    input.getLog().info("Starting server with the aim of retrieving the open api doc from configured endpoint.");
    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("org.springframework.boot"),
        artifactId("spring-boot-maven-plugin"), //https://docs.spring.io/spring-boot/docs/3.0.2/maven-plugin/reference/htmlsingle/#goals-start
        version(input.getPropertiesReader().getSpringBootVersion())//TODO: get values from PropertiesReader OR from implementing project's spring version?
      ),
      goal("start"),
      configuration(
          //element(name("profiles"), element(name("profile"), "")),TODO make is possible to configure a build profile in the GenerateDocConfig

        /*element(name("classesDirectory"), input.getProject().getBuild().getOutputDirectory()),
        element(name("directories"),String.join(",", input.getProject().getTestClasspathElements())),
        element(name("includes"),
          element(name("include"), //https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-tools/spring-boot-maven-plugin/src/main/java/org/springframework/boot/maven/Include.java
          element(name("groupId"),"org.springdoc"),
              element(name("artifactId"),"springdoc-openapi-starter-webflux-api") //TODO: if spring-boot-starter-webflux is on classpath use 'springdoc-openapi-starter-webflux-api' else if 'springdoc-openapi-starter-webmvc-api'. Also: if spring-security is on classpath then add 'springdoc-openapi-starter-common'?
            )
            ),*/
        element(name("arguments"),
          element(name("argument"), "--server.port="+ input.getProject().getProperties().getProperty("tomcat.http.port"))
        )
        //element(name("directories"), "C:/Users/m83522/.m2/repository/org/springframework/boot/spring-boot-starter-webflux/2.7.0") //TODO get dynamically a la ConditionalOnProperty.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      ),
      executionEnvironment(
        input.getProject(),
        input.getMavenSession(),
        input.getPluginManager()
      ));
  }

  private void getOpenApiDoc(DocumentGeneratorInput input) throws MojoExecutionException {
    String apiDocsUrl = "http://localhost:" + input.getProject().getProperties().getProperty("tomcat.http.port") + input.getGenerateDocConfig().getApiDocsUrl();
    input.getLog().info("Making localhost call to server to get open api json doc from the endpoint '"+apiDocsUrl+"' using the springdoc-openapi-maven-plugin.");

    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("org.springdoc"),
        artifactId("springdoc-openapi-maven-plugin"),//https://github.com/openapi-tools/swagger-maven-plugin
        version(input.getPropertiesReader().getSpringDocOpenApiMavenPluginVersion())//TODO: get values from PropertiesReader
      ),
      goal("generate"),
      configuration(
        element(name("outputFileName"), "swagger.json"),
        element(name("outputDir"), input.getOutputDir()),//"${project.build.directory}/openapi-spec"),
        element(name("apiDocsUrl"), apiDocsUrl)//Should match the implementing project's configured path: springdoc.api-docs.path=/v3/api-docs (default)
      ),
      executionEnvironment(
        input.getProject(),
        input.getMavenSession(),
        input.getPluginManager()
      ));
  }

  private void stopServer(DocumentGeneratorInput input) throws MojoExecutionException {
    input.getLog().info("Stopping server.");
    MojoExecutorWrapper.executeMojo(
      plugin(
        groupId("org.springframework.boot"),
        artifactId("spring-boot-maven-plugin"),//https://github.com/openapi-tools/swagger-maven-plugin
        version(input.getPropertiesReader().getSpringBootVersion())
      ),
      goal("stop"),
      configuration(
        //element(name("directories"), "C:/Users/m83522/.m2/repository/org/springframework/boot/spring-boot-starter-webflux/2.7.0") //TODO get dynamically a la ConditionalOnProperty.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      ),
      executionEnvironment(
        input.getProject(),
        input.getMavenSession(),
        input.getPluginManager()
      ));
  }

}
