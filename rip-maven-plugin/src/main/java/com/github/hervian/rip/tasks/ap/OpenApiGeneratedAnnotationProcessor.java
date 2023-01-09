package com.github.hervian.rip.tasks.ap;

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
@SupportedAnnotationTypes("javax.annotation.Generated")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class OpenApiGeneratedAnnotationProcessor extends AbstractProcessor {

  private ProcessingEnvironment processingEnvironment;
  private Elements elementUtils;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    System.out.println("OpenApiGeneratedAnnotationProcessor init......");
    super.init(processingEnvironment);
    this.processingEnvironment = processingEnvironment;
    this.elementUtils = processingEnvironment.getElementUtils();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    System.out.println("Entering processor...");
    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
      for (Element element: annotatedElements){
        Generated generatedAnno = element.getAnnotation(Generated.class);
        String[] value = generatedAnno.value();
        if ( value!=null && value.length>0 && value[0].equalsIgnoreCase(JavaClientCodegen.class.getName())) {
          PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(element);
          System.out.println(String.format("element simple name: %s, toString: %s, element.asType().toString: %s", element.getSimpleName(), element, element.asType().toString()));
        }
      }

    }
    /*if (roundEnv.processingOver() && !typesToEcco.isEmpty()){
      generateEccoClass(typesToEcco.get(0).getModule().getName());
    }*/
    return false;
  }
}
