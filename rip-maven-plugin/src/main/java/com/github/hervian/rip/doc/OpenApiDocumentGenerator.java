package com.github.hervian.rip.doc;

import org.apache.maven.plugin.MojoExecutionException;

public interface OpenApiDocumentGenerator {

  void generate(DocumentGeneratorInput documentGeneratorInput) throws MojoExecutionException;

}
