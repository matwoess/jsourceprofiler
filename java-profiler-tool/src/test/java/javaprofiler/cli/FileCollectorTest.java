package javaprofiler.cli;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.matwoess.jsourceprofiler.tool.cli.FileCollector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class FileCollectorTest {
  static Path baseDir, subDir;
  static Path mainJavaFile, subDirJavaFile, hiddenJavaFile, textFile, subDirXmlFile, moduleInfo, subDirPackageInfo;

  @BeforeAll
  public static void createDemoDirectoryStructure() throws IOException {
    // Create a directory structure with files to test the FileCollector class
    baseDir = Files.createTempDirectory("fileCollectorTestDir");
    mainJavaFile = Files.createTempFile(baseDir, "main", ".java");
    textFile = Files.createTempFile(baseDir, "textFile", ".txt");
    moduleInfo = Files.createTempFile(baseDir, "module-info", ".java");
    subDir = Files.createTempDirectory(baseDir, "subDir");
    subDirJavaFile = Files.createTempFile(subDir, "javaFile2", ".JAVA");
    subDirXmlFile = Files.createTempFile(subDir, "xmlFile", ".xml");
    subDirPackageInfo = Files.createTempFile(subDir, "package-info", ".java");
    Path hiddenDir = Files.createTempDirectory(baseDir, ".hiddenDir");
    hiddenJavaFile = Files.createTempFile(hiddenDir, "javaFile3", ".java");
  }

  private void assertSameFilesFound(List<Path> expected, List<Path> actual) {
    assertIterableEquals(expected.stream().sorted().toList(), actual.stream().sorted().toList());
  }

  @Test
  public void testAllJavaFiles() {
    List<Path> foundItems = new FileCollector(baseDir, "java", false).collect();
    List<Path> expectedItems = List.of(mainJavaFile, moduleInfo, subDirJavaFile, subDirPackageInfo, hiddenJavaFile);
    assertSameFilesFound(expectedItems, foundItems);
  }

  @Test
  public void testXmlFiles_uppercase_excludeNull() {
    List<Path> foundItems = new FileCollector(baseDir, "XML", false).excludePath(null).collect();
    List<Path> expectedItems = List.of(subDirXmlFile);
    assertSameFilesFound(expectedItems, foundItems);
  }

  @Test
  public void testJavaFiles_excludeHiddenDirectories_excludeMain() {
    List<Path> foundItems = new FileCollector(baseDir, "java", true)
        .excludePath(mainJavaFile)
        .collect();
    List<Path> expectedItems = List.of(moduleInfo, subDirJavaFile, subDirPackageInfo);
    assertSameFilesFound(expectedItems, foundItems);
  }

  @Test
  public void testJavaFiles_uppercaseExt_excludeInfoFiles() {
    List<Path> foundItems = new FileCollector(baseDir, "JAVA", false)
        .excludeFileName(moduleInfo.getFileName().toString())
        .excludeFileName(subDirPackageInfo.getFileName().toString())
        .collect();
    List<Path> expectedItems = List.of(mainJavaFile, subDirJavaFile, hiddenJavaFile);
    assertSameFilesFound(expectedItems, foundItems);
  }

  @Test
  public void testJavaFiles_excludeInfoFiles_noHiddenDirs_excludeMain() {
    List<Path> foundItems = new FileCollector(baseDir, "java", true)
        .excludeFileName(moduleInfo.getFileName().toString())
        .excludeFileName(subDirPackageInfo.getFileName().toString())
        .excludePath(mainJavaFile)
        .collect();
    List<Path> expectedItems = List.of(subDirJavaFile);
    assertSameFilesFound(expectedItems, foundItems);
  }

  @Test
  public void testJavaFiles_relativePaths_noHiddenDir() {
    List<Path> foundItems = new FileCollector(subDir.resolve(".."), "java", true)
        .excludeFileName(moduleInfo.getFileName().toString())
        .excludePath(subDirPackageInfo)
        .excludePath(mainJavaFile.resolve("..").resolve(mainJavaFile.getFileName()))
        .collect();
    List<Path> expectedItems = List.of(subDirJavaFile);
    assertSameFilesFound(expectedItems, foundItems);
  }
}
