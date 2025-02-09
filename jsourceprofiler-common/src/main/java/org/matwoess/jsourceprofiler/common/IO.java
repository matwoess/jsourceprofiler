package org.matwoess.jsourceprofiler.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Helper class containing methods related to relative path finding and filesystem operations.
 * <p>
 * The {@link #outputDir} is the most important member variable.
 * It specifies the root directory for all the tool's output files.
 * No other paths are statically defined.
 * All subdirectories and file locations can be retrieved by corresponding get methods,
 * that resolve their file path relative to the <code>outputDir</code> member.
 */
public class IO {
  /**
   * Represents the output directory for the profiler.
   * The output directory is used for storing instrumented files, compiled classes, and generated reports.
   * By default, the output directory is set to ".profiler" (of the current working directory).
   */
  public static Path outputDir = Path.of(".profiler");

  /**
   * Retrieves the current output directory for the profiler.
   *
   * @return the output directory path
   */
  public static Path getOutputDir() {
    return outputDir;
  }

  /**
   * Returns the <code>instrumented</code> directory relative to the {@link #outputDir}.
   *
   * @return the path of the instrumented directory
   */
  public static Path getInstrumentDir() {
    return getOutputDir().resolve("instrumented");
  }

  /**
   * Returns the <code>classes</code> directory relative to the {@link #outputDir}.
   *
   * @return the path of the classes directory
   */
  public static Path getClassesDir() {
    return getOutputDir().resolve("classes");
  }

  /**
   * Returns the path where the instrumented version of a Java source file is stored.
   * The tool will use this method to replicate the original directory structure for instrumented copies.
   *
   * @param relativePath the relative path of the original file to the sources directory.
   *                     Every <code>JavaFile</code> instance knows its relative location to the sources.
   * @return the path of an instrumented copy relative to the instrumented directory
   */
  public static Path getInstrumentedFilePath(Path relativePath) {
    return getInstrumentDir().resolve(relativePath);
  }

  /**
   * To successfully <b>compile</b> instrumented versions of source files, an import statement is added to all source files.
   * A <code>__Counter.class</code> file is placed in an "auxiliary" package directory to resolve these imports.
   *
   * @return where the pre-compiled <code>__Counter.class</code> class will be copied to before compilation
   */
  public static Path getAuxiliaryCounterInstrumentPath() {
    return getInstrumentDir().resolve("auxiliary").resolve("__Counter.class");
  }

  /**
   * To successfully <b>run</b> instrumented versions of source files, an import statement is added to all source files.
   * A <code>__Counter.class</code> file is placed in an auxiliary package directory to resolve this dependency.
   *
   * @return where the pre-compiled <code>__Counter.class</code> class will be copied to before running the instrumented code.
   */
  public static Path getAuxiliaryCounterClassPath() {
    return getClassesDir().resolve("auxiliary").resolve("__Counter.class");
  }

  /**
   * Returns the path to the parsed project's metadata file.
   * This data is used for instrumentation and report generation.
   *
   * @return <code>metadata.dat</code> relative to the {@link #outputDir}
   */
  public static Path getMetadataPath() {
    return getOutputDir().resolve("metadata.dat");
  }

  /**
   * Returns the path to the resulting counts file.
   * This data is written as soon as the instrumented version is run and is used for report generation.
   *
   * @return <code>counts.dat</code> relative to the {@link #outputDir}
   */
  public static Path getCountsPath() {
    return getOutputDir().resolve("counts.dat");
  }

  /**
   * Returns the containing root directory for the generated report.
   *
   * @return <code>report</code> relative to the {@link #outputDir}
   */
  public static Path getReportDir() {
    return getOutputDir().resolve("report");
  }

  /**
   * Returns the file path of the main report classes overview.
   *
   * @return <code>index.html</code> relative to the report directory
   */
  public static Path getReportIndexPath() {
    return getReportDir().resolve("index.html");
  }

  /**
   * Returns the path of a JavaScript or CSS report file relative to the report directory.
   *
   * @param reportResource the name of the resource file to be resolved against the report directory
   * @return the relative path of a source file in the report directory
   */
  public static Path getReportResourcePath(String reportResource) {
    return getReportDir().resolve(reportResource);
  }

  /**
   * Returns the target path for the symlink pointing to the main report index file.
   * On Windows, we create a shortcut instead of a symlink.
   * Therefore, the file extension <code>".lnk"</code> is added.
   *
   * @return the pre-defined link path relative to the current directory
   */
  public static Path getReportIndexSymLinkPath() {
    if (OS.getOS() == OS.WINDOWS) {
      return Path.of(".", "report.html.lnk");
    } else {
      return Path.of(".", "report.html");
    }
  }

  /**
   * Returns the path to the report method index of a java class.
   * Every parsed class gets its own one during report generation.
   *
   * @param className the name of a parsed java class
   * @return "index_" + <code>className</code> relative to the report directory
   */
  public static Path getReportMethodIndexPath(String className) {
    return IO.getReportDir().resolve("index_" + className + ".html");
  }

  /**
   * Returns the file path of an annotated HTML source file.
   * The relative path of the original source file to its sources directory should be passed to the method.
   * This way the package structure is replicated inside the <code>sources</code> report subdirectory.
   * The ".java" suffix is replaced by ".html" to create the new file name.
   *
   * @param relativePath the relative path from a <code>JavaFile</code> class relative to its sources directory
   * @return the location of the annotated report source code file
   */
  public static Path getReportSourceFilePath(Path relativePath) {
    Path reportFilePath = IO.getReportDir().resolve("source").resolve(relativePath);
    return reportFilePath.resolveSibling(reportFilePath.getFileName().toString().replace(".java", ".html"));
  }

  /**
   * {@return the home directory of the current user}
   */
  public static Path getUserHomeDir() {
    return Path.of(System.getProperty("user.home"));
  }

  /**
   * Returns where the configured FxUI parameters are persisted to inside the output directory.
   *
   * @return <code>parameters.dat</code> relative to the {@link #outputDir}
   */
  public static Path getUIParametersPath() {
    return getOutputDir().resolve("parameters.dat");
  }

  /**
   * Returns the path of the file for storing the previously opened project directory.
   * This is read when re-starting the FxUI application and pre-filled as the project to open.
   *
   * @return <code>lastProjectRootDirectory.txt</code> relative to the current directory
   */
  public static Path lastProjectPath() {
    return Path.of("lastProjectRootDirectory.txt");
  }

  /**
   * Extract a copy of a resource file from the project directory or the .jar file.
   *
   * @param <T>           the type of the resource class
   * @param resourceClass the anchor class used to locate the resource
   * @param resourceName  the name of the resource file to be extracted
   * @param destination   path where the resource is copied to
   */
  public static <T> void copyResource(Class<T> resourceClass, String resourceName, Path destination) {
    try (InputStream resource = resourceClass.getClassLoader().getResourceAsStream(resourceName)) {
      if (resource == null) {
        throw new RuntimeException("unable to locate resource: <" + resourceName + ">");
      }
      IO.createDirectoriesIfNotExists(destination);
      Files.copy(resource, destination, REPLACE_EXISTING);
    } catch (IOException | RuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Recursively extract a copy of a whole resource folder from the project directory or the .jar file.
   * Uses a temporary file system to access the contents of the JAR file if needed.
   *
   * @param resourceClass  the anchor class used to locate the resource
   * @param resourceFolder the name of the resource folder to be extracted
   * @param destination    folder path where the resource is copied to
   * @param <T>            the type of the resource class
   */
  public static <T> void copyResourceFolder(Class<T> resourceClass, String resourceFolder, Path destination) {
    try {
      var resourceUrl = resourceClass.getClassLoader().getResource(resourceFolder);
      if (resourceUrl == null) {
        throw new RuntimeException("Unable to locate resource folder: <" + resourceFolder + ">");
      }
      URI uri = resourceUrl.toURI();
      // The fileSystem variable is kept open to access the JAR contents
      try (FileSystem ignored = (uri.getScheme().equals("jar")) ? FileSystems.newFileSystem(uri, Collections.emptyMap()) : null) {
        Path resourcePath = Path.of(uri);
        if (!Files.isDirectory(resourcePath)) {
          throw new RuntimeException("Resource path is not a folder: <" + resourcePath + ">");
        }
        // Recursively copy the directory
        try (var paths = Files.walk(resourcePath)) {
          paths.forEach(sourcePath -> {
            var targetPath = destination.resolve(resourcePath.relativize(sourcePath).toString());
            try {
              if (Files.isDirectory(sourcePath)) {
                Files.createDirectories(targetPath);
              } else {
                Files.copy(sourcePath, targetPath, REPLACE_EXISTING);
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
        }
      }
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Deletes all files inside a directory.
   * Used to clean up output files before the next execution.
   * The root directory itself won't be removed to avoid locking errors.
   *
   * @param directory the root directory that should be cleared of contents recursively
   */
  public static void clearDirectoryContents(Path directory) {
    if (Files.exists(directory)) {
      try (Stream<Path> walk = Files.walk(directory)) {
        walk.sorted(Comparator.reverseOrder())
            .filter(p -> !p.equals(directory))
            .forEach(file -> {
              try {
                Files.delete(file);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Creates all parent directories in the path description to ensure successful creation of a new file in it.
   *
   * @param fileOrFolder if a file is specified, all the parent directories will be created.
   *                     If a directory is specified, all parent directories including the given one will be created.
   * @return whether the operation was successful
   */
  @SuppressWarnings("UnusedReturnValue")
  public static boolean createDirectoriesIfNotExists(Path fileOrFolder) {
    if (fileOrFolder.toFile().isDirectory()) {
      return fileOrFolder.toFile().mkdirs();
    } else {
      return fileOrFolder.getParent().toFile().mkdirs();
    }
  }

  /**
   * Creates a symbolic link at a given path pointing to another file path.
   * On Linux and macOS this is a soft link, on Windows it is a shortcut.
   * To create the Windows shortcut {@link #createWindowsShortcut(Path, Path)} is used.
   * The link extension must be ".lnk" in this case.
   *
   * @param link   the location of the linking file shortcut
   * @param target what the link should point to
   */
  public static void createLink(Path link, Path target) {
    try {
      if (Files.exists(link) && Files.isSymbolicLink(link)) {
        Files.delete(link);
      }
      if (OS.getOS() == OS.WINDOWS) {
        // create a shortcut on Windows using a PowerShell command
        createWindowsShortcut(link, target);
      } else {
        Files.createSymbolicLink(link, target);
      }
    } catch (IOException | RuntimeException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Creates a Windows shortcut at a given path pointing to another file.
   * The link file path extension must be ".lnk".
   *
   * @param link   the location of the linking file shortcut
   * @param target what the link should point to
   */
  private static void createWindowsShortcut(Path link, Path target) {
    if (!link.getFileName().toString().endsWith(".lnk")) {
      System.err.println("Unable to create shortcut. The path does not end with '.lnk': " + link);
      return;
    }
    String targetCanonical;
    try {
      targetCanonical = target.toFile().getCanonicalPath();
    } catch (IOException e) {
      System.err.println("Could not get canonical path for: " + target);
      return;
    }
    String[] command = new String[]{
        "powershell.exe",
        "-Command",
        "$WScriptShell = New-Object -ComObject WScript.Shell; "
            + "$Shortcut = $WScriptShell.CreateShortcut('" + link + "');"
            + "$Shortcut.TargetPath = '" + targetCanonical + "';"
            + "$Shortcut.Save();"
    };
    Util.runCommand(command);
    System.out.println("Report shortcut created at: " + link);
  }

  /**
   * Converts a <code>Path</code> object to a string with all forward slashes as a separator.
   * Heavily used during report generation.
   *
   * @param path the path to normalize
   * @return the string representation of a path with all "\" replaced by "/"
   */
  public static String normalize(Path path) {
    return path.toString().replace('\\', '/');
  }
}
