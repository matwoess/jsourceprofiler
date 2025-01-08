package org.matwoess.jsourceprofiler.common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for a <code>javac</code> command line with arguments.
 * Used as a common abstraction to avoid hard-coding an array of <code>javac</code> command
 * strings for <code>ProcessBuilder</code> usages.
 */
public class JCompilerCommandBuilder {
  private final List<String> sourceFiles = new ArrayList<>();
  private Path classPath;
  private Path directory;
  private final List<String> compileArgs = new ArrayList<>();

  /**
   * Register a source file that should be compiled.
   * @param sourceFile target source file to compile
   * @return the builder object itself for method chaining
   */
  public JCompilerCommandBuilder addSourceFile(Path sourceFile) {
    this.sourceFiles.add(sourceFile.toString());
    return this;
  }

  /**
   * Define the class path directory for the final <code>javac</code> command.
   * Will be appended using the <code>-cp</code> option.
   * @param classPath a directory that should be passed to the compiler as the class path option
   * @return the builder object itself for method chaining
   */
  public JCompilerCommandBuilder setClassPath(Path classPath) {
    this.classPath = classPath;
    return this;
  }

  /**
   * Define the output directory for the <code>javac</code> command, specified by the <code>-d</code> option.
   * Is used to place compiled <code>.class</code> files in a different directory than the instrumented files.
   * @param directory the directory path where <code>javac</code> outputs are placed in
   * @return the builder object itself for method chaining
   */
  public JCompilerCommandBuilder setDirectory(Path directory) {
    this.directory = directory;
    return this;
  }

  /**
   * Specify additional compile options that will be added to the final command.
   * <p>
   * The arg and value are always joined with <code>" "</code> together.
   * The method can be called multiple times.
   * Arguments will be added in-order.
   * @param arg the java compile argument name (example: -target)
   * @param val the value associated with the argument
   * @return the builder object itself for method chaining
   */
  public JCompilerCommandBuilder addCompileArg(String arg, String val) {
    compileArgs.add(arg);
    compileArgs.add(val);
    return this;
  }

  /**
   * Returns aan array of strings that can be joined to a valid <code>javac</code> command line.
   * <p>
   * The primary intended usage is to pass it to a {@link ProcessBuilder}.
   * This is commonly done with the {@link Util#runCommand} abstraction method.
   * @return the string array to form a <code>javac</code> command, built by the setter methods
   */
  public String[] build() {
    List<String> command = new ArrayList<>();
    command.add("javac");
    if (classPath != null) {
      command.add("-cp");
      command.add(classPath.toString());
    }
    if (directory != null) {
      command.add("-d");
      command.add(directory.toString());
    }
    command.addAll(compileArgs);
    command.addAll(sourceFiles);
    return command.toArray(String[]::new);
  }
}
