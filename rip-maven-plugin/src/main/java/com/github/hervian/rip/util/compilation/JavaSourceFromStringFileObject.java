package com.github.hervian.rip.util.compilation;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class JavaSourceFromStringFileObject extends SimpleJavaFileObject {
  final String code;

  public JavaSourceFromStringFileObject( String name, String code) {
    super( URI.create("string:///" + name.replace('.','/')
        + Kind.SOURCE.extension),Kind.SOURCE);
    this.code = code;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }
}
