package com.github.hervian.rip.client.installers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

public class DartClientInstaller implements ClientInstaller {

  /**
   * The generated Dart REST client can be installed into local system cache, just like what happens
   * when you run 'dart get ...'
   * Resource: https://dart.dev/tools/pub/cmd/pub-cache
   *
   * <pre>
   *
   * "PUB_CACHE
   * Some of pub’s dependencies are downloaded to the pub cache.
   * By default, this directory is located under .pub-cache in your home directory (on macOS and Linux),
   * or in %LOCALAPPDATA%\Pub\Cache (on Windows).
   * (The precise location of the cache may vary depending on the Windows version.)
   * You can use the PUB_CACHE environment variable to specify another location.
   * For more information, see The system package cache.
   *
   * Note: If you are attempting to use pub get behind a corporate firewall and it fails, please see pub get fails from behind a corporate firewall for information on how to set up the proxy environment variables for your platform."
   * Source: https://dart.dev/tools/pub/environment-variables
   * </pre>
   */
  @Override
  public ClientInstallerOutput install(ClientInstallerInput clientInstallerInput) throws MojoExecutionException {
    File src = new File(clientInstallerInput.getClientGeneratorOutput().getPath());

    String pubCache = System.getenv("PUB_CACHE");
    if (System.getenv("LOCALAPPDATA")!=null && !System.getenv("LOCALAPPDATA").trim().isEmpty()) {
      //We are on windows.
      pubCache = System.getenv("LOCALAPPDATA") + "/Pub/Cache";
    } else if (pubCache==null || pubCache.trim().isEmpty()) {
      pubCache = SystemUtils.getUserHome() + "/.pubCache";
    }
    String path = "/hosted/pub.dartlang.org/"; //TODO: get url from dart config similar to what is configured for the publisher.
    String name = (clientInstallerInput.getProject().getArtifactId() + "-client").replace("-", "_");//TODO: get value from ClientGenerator which really controls it.
    String version = clientInstallerInput.getProject().getVersion();
    String fileName = name + "-" + version;
    File dest = new File(pubCache + path + fileName);

    //TODO: delete me:
    System.out.println("inferred destination path to put dart client: " + dest.getAbsolutePath());

    try {
      FileUtils.copyDirectory(src, dest);
    } catch (IOException e) {
      throw  new MojoExecutionException("Error when trying to copy the generated dart client to the dart system cache folder.", e);
    }

    //dart pub cache add <package>
    /*String packageName = clientInstallerInput.getClientGeneratorOutput().getPath() + "/" + (clientInstallerInput.getProject().getArtifactId() + "-client").replace("-", "_");
    System.out.println("packageName =" + packageName);
    MojoExecutorWrapper.executeMojo(
        plugin(
            groupId("org.codehaus.mojo"),
            artifactId("exec-maven-plugin"),
            version(clientInstallerInput.getPropertiesReader().getExecMavenPluginVersion())
        ),
        goal("exec"),
        configuration(
            element(name("executable"), "dart"),
            element(name("arguments"),
                element(name("argument"), "pub"),
                element(name("argument"), "cache"),
                element(name("argument"), "add"),
                element(name("argument"), packageName)
            )
        ),
        executionEnvironment(
            clientInstallerInput.getProject(),
            clientInstallerInput.getMavenSession(),
            clientInstallerInput.getPluginManager()
        )
    );*/
    clientInstallerInput.getLog().info("Copying generated dart client to the dart system cache: " + dest.getAbsolutePath());
    return ClientInstallerOutput.builder().path(dest.getAbsolutePath()).build();//TODO: this is not the path to the package in system cache. How does one get that?
  }

}
