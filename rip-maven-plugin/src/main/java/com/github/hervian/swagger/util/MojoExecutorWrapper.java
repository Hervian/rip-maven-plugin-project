package com.github.hervian.swagger.util;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.Console;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class MojoExecutorWrapper {

  private static boolean securityManagerDoesNotAllowChangingStdOut = false;
  private static final Logger log = Logger.getLogger("");

  public static void executeMojo(Plugin plugin, String goal, Xpp3Dom configuration, MojoExecutor.ExecutionEnvironment env) throws MojoExecutionException {
    /*Console cnsl
      = System.console();
    cnsl.writer();*/



    PrintStream ps = System.out;
    if (!securityManagerDoesNotAllowChangingStdOut){
      try {
        TabPrependingLogger tabPrependingLogger = new TabPrependingLogger(System.out);
        System.setOut(tabPrependingLogger);
        System.out.println(String.format("\t| --------------- rip-maven-plugin invoking %s.%s:%s-------------------", plugin.getGroupId(), plugin.getArtifactId(), plugin.getGoals()));

       /* org.apache.logging.log4j.Logger rootLogger = LogManager.getRootLogger();*/
       /* LogManager.getRootLogger()
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();

        //TODO: if org.apache.log4j.Logger is on classpath then ... else Do nothing
        Enumeration<Appender> appenders = rootLogger==null ? null : rootLogger.getAllAppenders();
        if (appenders!=null)
        while (appenders.hasMoreElements()) {
          Appender appender = appenders.nextElement();
          System.out.println(appender.getName());
          System.out.println(appender.getClass());
        }*/

       /* Handler hOut = new ConsoleHandler(); // Snapshot of System.out
        log.addHandler(hOut);*/
        /*Handler hErr = new ConsoleHandler(); // Snapshot of System.err
        log.addHandler(hErr);*/
      } catch (SecurityException e){
        securityManagerDoesNotAllowChangingStdOut = true;
      }
    }

    MojoExecutor.executeMojo(plugin, goal, configuration, env);

    if (!securityManagerDoesNotAllowChangingStdOut){
      System.setOut(ps);
      System.out.println("------------------------------------------------------------------");

      /*Handler hOut = new ConsoleHandler(); // Snapshot of System.out
      log.addHandler(hOut);*/
      /*Handler hErr = new ConsoleHandler(); // Snapshot of System.err
      log.addHandler(hErr);*/
    }
  }

  /**
   * This only works partially. That is, it works for calls to System.out.println but not calls to the logger. Perhaps one needs to override more methods?
   * It could be that the Loggers ConsoleAppender caches the System.out variable at start up, i.e. it remains unaffected by the
   * edited System.out?
   */
  public static class TabPrependingLogger extends PrintStream {

    private static final String systemsNewline = System.getProperty("line.separator");

    public TabPrependingLogger(OutputStream out) {
      super(out);
    }

    @Override
    public void write(int b) {
      char newLine = systemsNewline.charAt(systemsNewline.length()-1);
      super.write(b);
      if (newLine==b) {
        super.write('\t');
      }
    }


    @Override
    public void print(String s) {
      super.print('\t' + s);
    }
    @Override
    public void println(String x) {
      super.println(x);
      super.write('\t');
      super.write('|');
      super.write(' ');
    }

    @Override
    public void print(char c) {
      super.print('\t');
      super.print(c);
    }
    @Override
    public void println(char c) {
      super.print('\t');
      super.println(c);
    }

    @Override
    public void print(char s[]) {
      super.print('\t');
      super.print(s);
    }
    @Override
    public void println(char[] x) {
      super.print('\t');
      super.println(x);
    }

    @Override
    public void print(Object obj) {
      super.print('\t');
      super.print(obj);
    }
    @Override
    public void println(Object x) {
      super.print('\t');
      super.println(x);
    }
  }

}
