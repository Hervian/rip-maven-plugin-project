package com.github.hervian.rip.util;

public class ClassPathHelper {

  public static boolean isOnClassPath(String fqcn) {
    try {
      Class.forName(fqcn);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
