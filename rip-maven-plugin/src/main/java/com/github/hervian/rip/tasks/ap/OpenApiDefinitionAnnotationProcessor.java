package com.github.hervian.rip.tasks.ap;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.openapitools.codegen.languages.JavaClientCodegen;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Should be executed programmatically from a Mojo bound to some pre-compile phase.
 */
@SupportedAnnotationTypes("io.swagger.v3.oas.annotations.OpenAPIDefinition")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class OpenApiDefinitionAnnotationProcessor extends AbstractProcessor {

  private ProcessingEnvironment processingEnvironment;
  private Elements elementUtils;
  public static boolean openApiDefinitionAnnotationIsPresentInCompilationUnit = false;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    System.out.println("OpenAPIDefinitionAnnotationProcessor init......");
    super.init(processingEnvironment);
    this.processingEnvironment = processingEnvironment;
    this.elementUtils = processingEnvironment.getElementUtils();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    System.out.println("Entering processor...");
    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
      openApiDefinitionAnnotationIsPresentInCompilationUnit = !annotatedElements.isEmpty();
    }
    /*if (roundEnv.processingOver() && !typesToEcco.isEmpty()){
      generateEccoClass(typesToEcco.get(0).getModule().getName());
    }*/
    return false;
  }
}
