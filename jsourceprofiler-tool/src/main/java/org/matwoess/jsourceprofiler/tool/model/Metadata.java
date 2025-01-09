package org.matwoess.jsourceprofiler.tool.model;

import org.matwoess.jsourceprofiler.common.IO;

import java.io.*;
import java.nio.file.Path;

/**
 * Helper class to represent the metadata of a java project.
 * <p>
 * Contains the total number of blocks and the list of java files and their respective {@link JavaFile} objects.
 * <p>
 * Includes methods to export and import the metadata to/from a file.
 *
 * @param blocksCount the total number of blocks found in the project
 * @param javaFiles   the list of java files in the project
 */
public record Metadata(int blocksCount, JavaFile[] javaFiles) {
  /**
   * Exports the metadata to the filesystem.
   * <p>
   * The metadata file is located at {@link IO#getMetadataPath()}.
   *
   * @param metadataPath the path to the metadata file
   */
  public void exportMetadata(Path metadataPath) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metadataPath.toFile()))) {
      oos.writeInt(blocksCount);
      oos.writeObject(javaFiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Imports the metadata from the filesystem.
   * <p>
   * The metadata file is located at {@link IO#getMetadataPath()}.
   *
   * @param metadataPath the path to the metadata file
   * @return a new {@link Metadata} object read from a file
   */
  public static Metadata importMetadata(Path metadataPath) {
    Metadata metadata;
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metadataPath.toFile()))) {
      int blocksCount = ois.readInt();
      JavaFile[] javaFiles = (JavaFile[]) ois.readObject();
      metadata = new Metadata(blocksCount, javaFiles);
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return metadata;
  }
}
