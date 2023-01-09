package com.github.hervian.swagger.generators.docs;

import org.apache.maven.plugin.MojoExecutionException;

public interface OpenApiDocumentGenerator {

  void generate(DocumentGeneratorInput documentGeneratorInput) throws MojoExecutionException;

}
