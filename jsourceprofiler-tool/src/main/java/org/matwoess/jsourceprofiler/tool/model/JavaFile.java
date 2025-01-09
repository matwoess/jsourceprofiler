package org.matwoess.jsourceprofiler.tool.model;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a java file.
 * <p>
 * Stores the package name, the top level classes and the found blocks.
 * <p>
 * It also stores the source file path and the relative path to the sources root.
 */
public class JavaFile implements Serializable {
  public int beginOfImports = 0;
  public String packageName;
  public List<JClass> topLevelClasses;
  public List<Block> foundBlocks;
  public transient Path sourceFile;
  public transient Path relativePath;

  /**
   * Creates a new JavaFile object based on a source file path and its parent root directory path.
   * <p>
   * The given source file will be stored as-is while the directory will be used
   * to determine a relative path between the directory and this source file.
   * This relative path is important to mirror the original project structure
   * in the instrumented directory and for the report file hierarchy.
   *
   * @param sourceFile the source file path
   * @param sourcesDir the top-level sources root directory
   */
  public JavaFile(Path sourceFile, Path sourcesDir) {
    this.sourceFile = sourceFile;
    Path fileAbsolute = sourceFile.toAbsolutePath().normalize();
    Path dirAbsolute = sourcesDir.toAbsolutePath().normalize();
    this.relativePath = dirAbsolute.relativize(fileAbsolute);
  }

  /**
   * Like {@link JavaFile#JavaFile(Path, Path)} but relative to the current working directory.
   * The relative path will be set to the file name itself.
   */
  public JavaFile(Path sourceFile) {
    this.sourceFile = sourceFile;
    this.relativePath = sourceFile.getFileName();
  }

  /**
   * Returns a list of all classes in this file, including inner classes recursively.
   *
   * @return the list of all classes in this file
   */
  public List<JClass> getClassesRecursive() {
    List<JClass> allClasses = new ArrayList<>(topLevelClasses);
    for (JClass clazz : topLevelClasses) {
      allClasses.addAll(clazz.getClassesRecursive());
    }
    return allClasses;
  }

  /**
   * Custom serialization method, storing paths as strings.
   *
   * @param oos the output stream
   * @throws IOException if any IO error occurs
   */
  @Serial
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    oos.writeUTF(sourceFile.toString());
    oos.writeUTF(relativePath.toString());
  }

  /**
   * Custom deserialization method, reading paths from strings.
   *
   * @param ois the input stream
   * @throws IOException            if any IO error occurs
   * @throws ClassNotFoundException if any class cannot be found
   */
  @Serial
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    sourceFile = Paths.get(ois.readUTF());
    relativePath = Paths.get(ois.readUTF());
  }
}
