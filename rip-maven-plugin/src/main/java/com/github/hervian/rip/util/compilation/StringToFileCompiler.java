package com.github.hervian.rip.util.compilation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Code is based on https://stackoverflow.com/a/17526282/6095334
 */
public class StringToFileCompiler {
  private static final Logger LOGGER = LoggerFactory.getLogger(StringToFileCompiler.class);

  public static boolean compile(String fileName, String code, String targetFolder, List<Class<?>> restAnnotationTypes) throws Exception {
    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
    if( jc == null) throw new Exception( "Compiler unavailable");
    JavaSourceFromStringFileObject jsfs = new JavaSourceFromStringFileObject(fileName, code);

    Iterable<? extends JavaFileObject> fileObjects = Arrays.asList( jsfs);

    List<String> options = new ArrayList<String>();
    options.add("-d");
    options.add( targetFolder);
    //See https://github.com/thought-machine/please-java/blob/master/javac_worker/src/build/please/compile/JavaCompiler.java
    options.add("--module-path");

    URLClassLoader urlClassLoader =
        (URLClassLoader)Thread.currentThread().getContextClassLoader();
    StringBuilder sb = new StringBuilder();
    for (URL url : urlClassLoader.getURLs()) {
      sb.append(url.getFile()).append(File.pathSeparator);
    }

    List<String> jarPathsToAddToModulePath = restAnnotationTypes.stream().map(e -> e.getProtectionDomain().getCodeSource().getLocation().getPath()).collect(Collectors.toList());

    // pathSeparator = isWindows ? ";" : ":";
    String path = String.join(File.pathSeparator, jarPathsToAddToModulePath);

   /* String path = GET.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    path += ";" + Operation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    path += ";" + ConditionalOnProperty.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    path += ";" + Component.class.getProtectionDomain().getCodeSource().getLocation().getPath();*/

    path = path.replace(";/", ";");
    if (path.startsWith("/")){
      path = path.substring(1);
    }
    options.add(path);
    //System.out.println("module-path:\n" + path);

    options.add("--add-modules");
    options.add("java.ws.rs,com.fasterxml.jackson.databind"); //Can any of these be used instead? ALL-DEFAULT, ALL-SYSTEM, and ALL-MODULE-PATH

    /*System.out.println("Modules:");
    ModuleLayer.boot().modules().stream()
      .map(Module::getName)
      .forEach(System.out::println);*/

    StringWriter output = new StringWriter();
    //Alternatively: https://blog.frankel.ch/compilation-java-code-on-the-fly/
    boolean success = jc.getTask( output, null, null, options, null, fileObjects).call();


    if( success) {
      LOGGER.info( "Class has been successfully compiled");
      return success;
    } else {
      throw new Exception( "Compilation failed :" + output);
    }
  }

}
