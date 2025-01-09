package org.matwoess.jsourceprofiler.common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for a <code>java</code> command line with arguments.
 * Used as a common abstraction to avoid hard-coding an array of <code>java</code> command
 * strings for <code>ProcessBuilder</code> usages.
 */
public class JavaCommandBuilder {
  private String mainClass;
  private Path classPath;
  private final List<String> args = new ArrayList<>();

  /**
   * Register the main class name, later added as the first argument after options.
   * @param mainClass the qualified name of the compiled class containing the main entry point
   * @return the builder object itself for method chaining
   */
  public JavaCommandBuilder setMainClass(String mainClass) {
    this.mainClass = mainClass;
    return this;
  }

  /**
   * Register the class path that will be passed to the <code>java</code> command with the <code>-cp</code> argument.
   * @param classPath a folder or JAR-file containing compiled classes used in the program
   * @return the builder object itself for method chaining
   */
  public JavaCommandBuilder setClassPath(Path classPath) {
    this.classPath = classPath;
    return this;
  }

  /**
   * Registers additional arguments for the executed program.
   * An internal list is kept and appended in order of specification.
   * Will be appended to the <code>java</code> command after the main class.
   * It can be called multiple times.
   * @param args varargs array of string arguments
   * @return the builder object itself for method chaining
   */
  public JavaCommandBuilder addArgs(String... args) {
    if (args != null) {
      this.args.addAll(List.of(args));
    }
    return this;
  }

  /**
   * Returns an array of strings that can be joined to a valid <code>java</code> command line.
   * <p>
   * The primary intended usage is to pass it to a {@link ProcessBuilder}.
   * This is commonly done with the {@link Util#runCommand} abstraction method.
   * @return the string array to form a <code>java</code> command, built by the setter methods
   */
  public String[] build() {
    List<String> command = new ArrayList<>();
    command.add("java");
    if (classPath != null) {
      command.add("-cp");
      command.add(classPath.toString());
    }
    command.add(mainClass);
    command.addAll(args);
    return command.toArray(String[]::new);
  }
}
