package org.matwoess.jsourceprofiler.tool.cli;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects files with a specific file extension from a directory and its subdirectories.
 * <p>
 * The collected files can be filtered by excluding specific file names and paths.
 * Whether to include hidden directories can be specified.
 */
public class FileCollector {
  final Path baseDirectory;
  final String fileExtension;
  final boolean excludeHiddenDirs;
  final List<String> nameExclusions = new ArrayList<>();
  final List<Path> pathExclusions = new ArrayList<>();

  /**
   * Creates a new FileCollector for the given base directory and file extension.
   * <p>
   * The file extension is case-insensitive and should not contain a leading dot.
   *
   * @param baseDir           the base directory to start the search from
   * @param fileExtension     the file extension to collect files for
   * @param excludeHiddenDirs whether to exclude hidden directories from the search
   */
  public FileCollector(Path baseDir, String fileExtension, boolean excludeHiddenDirs) {
    this.baseDirectory = baseDir.normalize();
    this.fileExtension = "." + fileExtension.toLowerCase();
    this.excludeHiddenDirs = excludeHiddenDirs;
  }

  /**
   * Adds a file name to the list of exclusions.
   *
   * @param fileName the file name to exclude
   * @return this FileCollector instance
   */
  public FileCollector excludeFileName(String fileName) {
    nameExclusions.add(fileName);
    return this;
  }

  /**
   * Adds a path to the list of exclusions.
   *
   * @param exclusion the path to exclude
   * @return this FileCollector instance
   */
  public FileCollector excludePath(Path exclusion) {
    if (exclusion != null) {
      pathExclusions.add(exclusion.toAbsolutePath().normalize());
    }
    return this;
  }

  /**
   * Collects the files from the base directory and its subdirectories
   * using <code>Files.walkFileTree</code> and a custom visitor.
   * The collected files are filtered by the file extension and the exclusion lists.
   * Hidden directory tree paths are excluded if specified.
   *
   * @return a list of paths to the collected files
   */
  public List<Path> collect() {
    final List<Path> files = new ArrayList<>();
    try {
      Files.walkFileTree(baseDirectory, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
          if (excludeHiddenDirs) {
            String dirName = dir.getFileName().toString();
            if (dirName.startsWith(".") && !dirName.startsWith("..")) {
              return FileVisitResult.SKIP_SUBTREE;
            }
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          String fileName = file.getFileName().toString();
          if (fileName.toLowerCase().endsWith(fileExtension)
              && !nameExclusions.contains(fileName)
              && !pathExclusions.contains(file.toAbsolutePath().normalize())) {
            files.add(file);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      throw new RuntimeException("Error while scanning directory for files", e);
    }
    return files;
  }
}
