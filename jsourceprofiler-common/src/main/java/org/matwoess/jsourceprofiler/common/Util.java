package org.matwoess.jsourceprofiler.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides utility methods for all modules to use.
 */
public class Util {

  /**
   * Extend an array by prepending a number of values with the same type to it.
   * <p>
   * First an array is created with the size of the combined lengths.
   * Then the prepend-values are copied to it, followed by copying the original array values.
   *
   * @param array         the original array to be extended
   * @param prependValues vararg array of values that will be prepended to it
   * @param <T>           the common type of both arrays
   * @return the extended array containing the <code>prependValues</code> followed by the original array values
   */
  @SafeVarargs
  public static <T> T[] prependToArray(T[] array, T... prependValues) {
    T[] extendedArray = Arrays.copyOf(prependValues, prependValues.length + array.length);
    System.arraycopy(array, 0, extendedArray, prependValues.length, array.length);
    return extendedArray;
  }

  /**
   * Checks whether a file path has a ".java" extension and is a file (not a directory).
   *
   * @param path the path to be checked
   * @return whether the specified path is a regular file and has a ".java" extension
   */
  public static boolean isJavaFile(Path path) {
    return path.toString().endsWith(".java") && Files.isRegularFile(path);
  }

  /**
   * Checks whether a child path starts with a given directory path.
   * Also returns <code>true</code> if the child is a file directly located in the <code>ancestorDir</code>.
   * Will return <code>false</code> if both paths are directories and match exactly.
   *
   * @param ancestorDir the ancestor path to check against
   * @param childPath   the path to check whether it is a descendant
   * @return whether the child path is located somewhere inside the parent path
   */
  public static boolean isAncestorOf(Path ancestorDir, Path childPath) {
    File parentFile = ancestorDir.toFile();
    if (!parentFile.isDirectory()) {
      return false; // a file path cannot contain lower level files or directories
    }
    final String canonicalParentPath;
    final String canonicalChildPath;
    try {
      canonicalParentPath = parentFile.getCanonicalPath();
    } catch (IOException e) {
      System.err.println("Could not get canonical path for " + ancestorDir);
      return false;
    }
    try {
      canonicalChildPath = childPath.toFile().getCanonicalPath();
    } catch (IOException e) {
      System.err.println("Could not get canonical path for " + childPath);
      return false;
    }
    return !canonicalChildPath.equals(canonicalParentPath) && canonicalChildPath.startsWith(canonicalParentPath);
  }


  /**
   * Run a generic command using the system command line.
   * <p>
   * The {@link ProcessBuilder} class will be used to execute it.
   *
   * @param command an array of strings forming a command by joining it with <code>" "</code>
   * @return the exit code of the executed command
   */
  public static int runCommand(String... command) {
    return runCommandInDir(null, command);
  }

  /**
   * Runs a command line in a specified working directory.
   * <p>
   * like {@link #runCommand} but with a directory to execute from.
   *
   * @param cwd     the current working directory that will be passed to the <code>ProcessBuilder</code>
   * @param command the array of strings forming the command
   * @return the exit code of the executed command
   */
  public static int runCommandInDir(Path cwd, String... command) {
    ProcessBuilder builder = new ProcessBuilder()
        .inheritIO()
        .command(command);
    if (cwd != null) {
      builder.directory(cwd.toFile());
    }
    try {
      Process process = builder.start();
      return process.waitFor();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Run a command line in a specified directory and get the string output as a return value.
   *
   * @param cwd     the current working directory that will be passed to the <code>ProcessBuilder</code>
   * @param command the array of strings forming the command
   * @return an array of strings representing the output of the executed command
   */
  public static String[] runCommandAndGetOutput(Path cwd, String... command) {
    ProcessBuilder builder = new ProcessBuilder().directory(cwd.toFile()).command(command);
    List<String> output = new ArrayList<>();
    try {
      Process process = builder.start();
      BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      process.waitFor();
      stdIn.lines().forEach(output::add);
      stdErr.lines().forEach(output::add);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    return output.toArray(String[]::new);
  }

}
