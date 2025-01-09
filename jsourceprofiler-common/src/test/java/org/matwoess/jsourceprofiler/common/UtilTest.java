package org.matwoess.jsourceprofiler.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilTest {
  final Path samplesFolder = Path.of("..", "sample");
  final Path simpleExampleFile = samplesFolder.resolve("Simple.java");
  final Path helperExampleFile = samplesFolder.resolve("helper").resolve("Helper.java");
  final Path shoppingListFile = samplesFolder.resolve("files").resolve("shopping-list.xml");

  @Test
  public void testIsJavaFile() {
    assertTrue(Util.isJavaFile(simpleExampleFile));
    assertFalse(Util.isJavaFile(shoppingListFile));
    assertFalse(Util.isJavaFile(samplesFolder));
  }

  @Test
  public void testIsAncestorOf() {
    // direct child
    assertTrue(Util.isAncestorOf(samplesFolder, simpleExampleFile));
    // lower level child
    assertTrue(Util.isAncestorOf(samplesFolder, helperExampleFile));
    // ancestor equals child, both are directories
    assertFalse(Util.isAncestorOf(samplesFolder, samplesFolder));
    // ancestor equals child, both are files
    assertFalse(Util.isAncestorOf(simpleExampleFile, simpleExampleFile));
    // lower level child, not a java file
    assertTrue(Util.isAncestorOf(samplesFolder, shoppingListFile));
    // ancestor is not a directory
    assertFalse(Util.isAncestorOf(simpleExampleFile, simpleExampleFile.resolve("any")));
    // child above parent
    assertFalse(Util.isAncestorOf(samplesFolder, samplesFolder.getParent()));
    // file directly in "." (current directory)
    assertTrue(Util.isAncestorOf(Path.of("."), Path.of("file.java")));
  }
}
