package com.github.hervian.rip;

import com.github.hervian.rip.config.PropertiesReader;
import com.github.hervian.rip.config.DiffConfig;
import com.github.hervian.rip.util.MojoExecutorWrapper;
import lombok.Data;
import lombok.Getter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.List;

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
 * Fails the build if the pom file's SemVer version number's major has NOT increased BUT a breaking change has been introduced in the REST API.
 * That is, this mojo compares the current code's swagger.json with that of the inferred previous version.
 *
 * NB: Current version of this mojo expects the new api to be available at a specific, non-configurable path, namely the
 * one in which the generateDoc plugin geneates the swagger.json. TODO: Fix
 */
@Data
@Mojo(name = "diff",
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    threadSafe = true)
public class DiffMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Component
  private MavenSession mavenSession;

  @Component
  private BuildPluginManager pluginManager;

  @Parameter
  private DiffConfig diffConfig;

  @Getter
  private PropertiesReader propertiesReader;

  public DiffMojo() throws MojoExecutionException {
    try {
      propertiesReader = new PropertiesReader();
    } catch (IOException e) {
      throw new MojoExecutionException("Error when constructing PropertiesReader object used to read properties from pom file.", e);
    }
  }

  @Override
  public void execute() throws MojoExecutionException {
    if (!diffConfig.isSkipCheckForBreakingChanges()){
      String packaging = project.getPackaging();
      System.out.println("packaging: "+ packaging);
      failBuildOnUnflaggedBreakingChanges();
    }
  }

  /**
   * See if below changes (major version stuff) can be solved by plugin devs - https://github.com/redskap/swagger-brake-maven-plugin/issues/23
   * Call swagger-brake-maven-plugin to check for breaking changes (if skipCheckForBreakingChanges=false)
   * Break the build if all of the following is true:
   * <ol>
   *   <li>The flag skipCheckForBreakingChanges is false (default is false)</li>
   *   <li>The swagger-brake-maven-plugin reports a breaking change</li>
   *   <li>The pom version have not had its major version increased</li>
   * </ol>
   *
   * Sources: https://arnoldgalovics.com/introducing-swagger-brake/
   *
   * @throws MojoExecutionException
   */
  private void failBuildOnUnflaggedBreakingChanges() throws MojoExecutionException {
    /*AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.scan("com.zetcode");
    ctx.refresh();

    var bean = ctx.getBean(Application.class);
    bean.run();

    ctx.close();
    LatestArtifactVersionResolver latestArtifactVersionResolver =
    String getPreviousVersion = latestArtifactVersionResolver.resolve(options);*/
    getLog().info("Calling swagger-brake-maven-plugin to check if there are breaking API changes that have not been reflected in the pom's version (semantic versioning).");
    String releaseRepository = null, snapshotRepository = null;
    for (org.apache.maven.model.Repository repository: (List<Repository>)project.getRepositories()){
      System.out.println("repository:" + repository);
      if (repository.getReleases()!=null && repository.getReleases().isEnabled()){
        System.out.println(repository.getUrl());
        releaseRepository = repository.getUrl();
      }
      if (repository.getSnapshots()!=null && repository.getSnapshots().isEnabled()){
        System.out.println(repository.getUrl());
        snapshotRepository = repository.getUrl();
      }
    }
    System.out.println("Using packaging: " + project.getPackaging());
    MojoExecutorWrapper.executeMojo(
        plugin(
            groupId("io.redskap"),
            artifactId("swagger-brake-maven-plugin"),
            version(propertiesReader.getSwaggerBrakeVersion())
        ),
        goal("check"),
        configuration(
            element(name("newApi"), project.getBuild().getOutputDirectory()+"/swagger/swagger.json"),
            element(name("mavenRepoUrl"), getReleasesRepo(project)),//releaseRepository),
            element(name("mavenSnapshotRepoUrl"), getSnapshotRepo(project))//snapshotRepository)
        ),
        executionEnvironment(
            project,
            mavenSession,
            pluginManager
        )
    );
    //TODO: Probably the plugin will throw an exception which will then be wrapped in a MojoExecutionEx
    //In case no jar has been pushed remote yet, this will likely fail.
    // We should ignore such failures. See GenerateClientMojo that catches LatestArtifactDownloadException.
  }

  public static String getReleasesRepo(MavenProject project){
    String releaseRepository = null;
    for (org.apache.maven.model.Repository repository: (List<org.apache.maven.model.Repository>)project.getRepositories()){
      if (repository.getReleases()!=null && repository.getReleases().isEnabled()){
        System.out.println(repository.getUrl());
        releaseRepository = repository.getUrl();
      }
    }
    return releaseRepository;
  }

  public static String getSnapshotRepo(MavenProject project){
    String snapshotRepository = null;
    for (org.apache.maven.model.Repository repository: (List<org.apache.maven.model.Repository>)project.getRepositories()){
      if (repository.getSnapshots()!=null && repository.getSnapshots().isEnabled()){
        System.out.println(repository.getUrl());
        snapshotRepository = repository.getUrl();
      }
    }
    return snapshotRepository;
  }

}
