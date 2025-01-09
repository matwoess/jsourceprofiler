package org.matwoess.jsourceprofiler.tool.cli;

import org.matwoess.jsourceprofiler.common.Util;
import org.matwoess.jsourceprofiler.tool.instrument.Instrumenter;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;
import org.matwoess.jsourceprofiler.tool.profile.Profiler;

import java.nio.file.Path;

/**
 * Main class of the profiler tool.
 * <p>
 * Parses the command line arguments and calls the appropriate methods.
 */
public class Main {
  /**
   * Main entry point of the profiler tool.
   *
   * @param args the command line arguments specifying options and the main file to profile
   */
  public static void main(String[] args) {
    Arguments arguments = null;
    try {
      arguments = Arguments.parse(args);
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.out.println("Use -h for help.");
      System.exit(1);
    }
    if (arguments == null) return;
    switch (arguments.runMode()) {
      case REPORT_ONLY -> generateReportOnly();
      case INSTRUMENT_ONLY -> instrumentOnly(arguments);
      case DEFAULT -> instrumentCompileAndRun(arguments);
    }
  }

  private static void generateReportOnly() {
    Profiler profiler = new Profiler(null);
    profiler.generateReport();
    profiler.createLinkForReport();
  }

  private static void instrumentOnly(Arguments arguments) {
    JavaFile[] javaFiles;
    if (arguments.targetPath().toFile().isFile()) {
      javaFiles = new JavaFile[]{new JavaFile(arguments.targetPath())};
    } else {
      javaFiles = getJavaFilesInFolder(arguments.targetPath(), null);
    }
    Instrumenter instrumenter = new Instrumenter(javaFiles, arguments);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
  }

  private static void instrumentCompileAndRun(Arguments arguments) {
    JavaFile mainJavaFile;
    JavaFile[] additionalJavaFiles = new JavaFile[0];
    if (arguments.sourcesDir() != null) {
      mainJavaFile = new JavaFile(arguments.targetPath(), arguments.sourcesDir());
      additionalJavaFiles = getJavaFilesInFolder(arguments.sourcesDir(), arguments.targetPath());
    } else {
      mainJavaFile = new JavaFile(arguments.targetPath());
    }
    JavaFile[] allJavaFiles = Util.prependToArray(additionalJavaFiles, mainJavaFile);
    Instrumenter instrumenter = new Instrumenter(allJavaFiles, arguments);
    instrumenter.analyzeFiles();
    instrumenter.instrumentFiles();
    instrumenter.exportMetadata();
    Profiler profiler = new Profiler(mainJavaFile, additionalJavaFiles);
    profiler.compileInstrumented();
    profiler.profile(arguments.programArgs());
    profiler.generateReport();
    profiler.createLinkForReport();
  }

  private static JavaFile[] getJavaFilesInFolder(Path sourcesFolder, Path exceptFor) {
    return new FileCollector(sourcesFolder, "java", true)
        .excludeFileName("package-info.java")
        .excludeFileName("module-info.java")
        .excludePath(exceptFor)
        .collect()
        .stream()
        .map(sourceFile -> new JavaFile(sourceFile, sourcesFolder))
        .toArray(JavaFile[]::new);
  }
}
