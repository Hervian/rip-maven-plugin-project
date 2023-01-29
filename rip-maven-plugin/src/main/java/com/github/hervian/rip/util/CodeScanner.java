package com.github.hervian.rip.util;

import com.github.hervian.rip.doc.src.OpenApiConfig;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.apache.maven.project.MavenProject;
import org.reflections.util.ClasspathHelper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodeScanner {

  public static List<String> findOpenAPIDefinitionAnnotations(MavenProject project) {
    ClassLoader classLoader = getClassLoader(project);
    List<String> foundTypes;
    try (ScanResult scanResult = new ClassGraph().enableClassInfo().overrideClasspath(ClasspathHelper.forClassLoader(classLoader)).enableAnnotationInfo().scan()) {
      foundTypes = scanResult.getClassesWithAnnotation(OpenAPIDefinition.class.getName()).getNames().stream().filter(e -> !e.equals(OpenApiConfig.class.getName())).collect(Collectors.toList());
    }
    return foundTypes;
  }

  private static final ClassLoader getClassLoader(MavenProject project) {
    System.out.println("*************************************************************");
    final List<String> classpathElements = new ArrayList<>();
    classpathElements.add(project.getBuild().getOutputDirectory());
    final List<URL> projectClasspathList = new ArrayList<>();
    for (final String element : classpathElements) {
      try {
        URL url = new File(element).toURI().toURL();
        projectClasspathList.add(url);
        System.out.println(url);
      } catch (final MalformedURLException ex) {
        throw new RuntimeException(
            element + " is an invalid classpath element", ex
        );
      }
    }
    return new URLClassLoader(
        projectClasspathList.toArray(new URL[projectClasspathList.size()]),
        Thread.currentThread().getContextClassLoader()
    );
  }

}
