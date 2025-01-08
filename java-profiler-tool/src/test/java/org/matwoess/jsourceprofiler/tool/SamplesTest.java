package org.matwoess.jsourceprofiler.tool;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.common.JCompilerCommandBuilder;
import org.matwoess.jsourceprofiler.common.JavaCommandBuilder;
import org.matwoess.jsourceprofiler.common.Util;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SamplesTest {
  final Path samplesFolder = Path.of("..", "sample");

  @Test
  public void testSimpleSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Simple.java"));
  }

  @Test
  public void testSimpleSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Simple.java");
  }

  @Test
  public void testBasicElementsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("BasicElements.java"));
  }

  @Test
  public void testBasicElementsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "BasicElements.java");
  }

  @Test
  public void testStringsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Strings.java"));
  }

  @Test
  public void testStringsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Strings.java");
  }

  @Test
  public void testClassesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Classes.java"));
  }

  @Test
  public void testClassesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Classes.java");
  }

  @Test
  public void testInterfacesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Interfaces.java"));
  }

  @Test
  public void testInterfacesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Interfaces.java");
  }

  @Test
  public void testEnumsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Enums.java"));
  }

  @Test
  public void testEnumsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Enums.java");
  }

  @Test
  public void testMissingBracesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("MissingBraces.java"));
  }

  @Test
  public void testMissingBracesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "MissingBraces.java");
  }

  @Test
  public void testPackagesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("Packages.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void testPackagesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Packages.java");
  }

  @Test
  public void testSwitchesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Switches.java"));
  }

  @Test
  public void testSwitchesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Switches.java");
  }

  @Test
  public void testLambdasSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Lambdas.java"));
  }

  @Test
  public void testLambdasSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Lambdas.java");
  }

  @Test
  public void testAnonymousClassesSample_MissingDependencies() {
    Path mainFile = samplesFolder.resolve("AnonymousClasses.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void testAnonymousClassesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "AnonymousClasses.java");
  }

  @Test
  public void testAnnotationsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Annotations.java"));
  }

  @Test
  public void testAnnotationsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Annotations.java");
  }

  @Test
  public void testAllSamplesSample() {
    Path mainFile = samplesFolder.resolve("AllSamples.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertTrue(ex.getMessage().contains("Error compiling instrumented file"));
  }

  @Test
  public void testAllSamplesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "AllSamples.java");
  }


  @Test
  public void testHelperSample_NoMainClass() {
    Path mainFile = samplesFolder.resolve("helper").resolve("Helper.java");
    RuntimeException ex = assertThrows(RuntimeException.class, () -> TestUtils.instrumentAndProfile(mainFile));
    assertEquals("Error executing compiled class: Helper", ex.getMessage());
  }

  @Test
  public void testNestedBlockTypesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("NestedBlockTypes.java"));
  }

  @Test
  public void testNestedBlockTypesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "NestedBlockTypes.java");
  }

  @Test
  public void testLocalClassesSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("LocalClasses.java"));
  }

  @Test
  public void testLocalClassesSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "LocalClasses.java");
  }

  @Test
  public void testRecordsSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("Records.java"));
  }

  @Test
  public void testRecordsSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "Records.java");
  }


  @Test
  public void testControlBreaksSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("ControlBreaks.java"));
  }

  @Test
  public void testControlBreaksSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "ControlBreaks.java");
  }

  @Test
  public void testLocalFilesSample() throws IOException {
    Path mainFile = samplesFolder.resolve("LocalFiles.java");
    Path priceTotalFile = samplesFolder.resolve("files").resolve("total.txt");
    TestUtils.instrumentAndProfileWithArgs(mainFile.toString(), priceTotalFile.toString());
    assertTrue(priceTotalFile.toFile().exists());
    assertEquals(Double.parseDouble(Files.readString(priceTotalFile)), 5.78, 0.001);
  }

  @Test
  public void testSwitchPatternMatchingSample() {
    TestUtils.instrumentAndProfile(samplesFolder.resolve("SwitchPatternMatching.java"));
  }

  @Test
  public void testSwitchPatternMatchingSample_Folder() {
    TestUtils.instrumentFolderAndProfile(samplesFolder, "SwitchPatternMatching.java");
  }

  @Test
  public void testParallelSumSample() {
    Path mainFile = samplesFolder.resolve("ParallelSum.java");
    TestUtils.instrumentAndProfileWithArgs(mainFile.toString(), String.valueOf(5_000_000), "4");
  }

  @Test
  public void testParallelSumSample_SyncCounters() {
    Path mainFile = samplesFolder.resolve("ParallelSum.java");
    TestUtils.instrumentAndProfileWithArgs("--synchronized", mainFile.toString(), String.valueOf(5_000_000), "4");
  }

  @Test
  public void testParallelSumSample_noCounters() {
    Path mainFile = samplesFolder.resolve("ParallelSum.java");
    Util.runCommand(new JCompilerCommandBuilder()
        .setDirectory(IO.getInstrumentDir())
        .addSourceFile(mainFile)
        .build());
    Util.runCommand(new JavaCommandBuilder()
        .setClassPath(IO.getInstrumentDir())
        .setMainClass("ParallelSum")
        .addArgs(String.valueOf(5_000_000), "4")
        .build());
  }
}
