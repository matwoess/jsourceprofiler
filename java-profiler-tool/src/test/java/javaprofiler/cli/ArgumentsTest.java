package javaprofiler.cli;

import org.matwoess.jsourceprofiler.common.OS;
import org.matwoess.jsourceprofiler.common.RunMode;
import org.junit.jupiter.api.Test;
import org.matwoess.jsourceprofiler.tool.cli.Arguments;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.matwoess.jsourceprofiler.tool.cli.Arguments.getUsage;

public class ArgumentsTest {
  final Path samplesFolder = Path.of("..", "sample");
  final Path simpleExampleFile = samplesFolder.resolve("Simple.java");
  final Path lambdaExampleFile = samplesFolder.resolve("Lambdas.java");
  final Path algorithmsExampleFile = samplesFolder.resolve("Algorithms.java");
  final Path shoppingListFile = samplesFolder.resolve("files").resolve("shopping-list.xml");
  final Path foreignJavaFile;

  {
    try {
      foreignJavaFile = Files.createTempFile("foreign", ".java");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testShowUsage_noError() {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    Arguments.parse(new String[]{"-h"});
    String newline = OS.getOS().lineSeparator();
    assertEquals(getUsage() + newline, outContent.toString());
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    Arguments.parse(new String[]{"--help"});
    assertEquals(getUsage() + newline, outContent.toString());
    // not as first argument
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    Arguments.parse(new String[]{"-s", "-v", "-d", samplesFolder.toString(), "--help"});
    assertEquals(getUsage() + newline, outContent.toString());
    System.setOut(originalOut);
  }

  @Test
  public void testNoArguments() {
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(new String[0]));
    assertEquals("No arguments specified.", exception.getMessage());
  }

  @Test
  public void testDefaultMode() {
    Arguments args = Arguments.parse(new String[]{simpleExampleFile.toString()});
    Arguments expected = new Arguments(RunMode.DEFAULT, simpleExampleFile, null, false, false, null);
    assertEquals(expected, args);
  }

  @Test
  public void testDefaultMode_withFolder() {
    Arguments args = Arguments.parse(new String[]{"-d", samplesFolder.toString(), simpleExampleFile.toString()});
    Arguments expected = new Arguments(RunMode.DEFAULT, simpleExampleFile, samplesFolder, false, false, null);
    assertEquals(expected, args);
  }

  @Test
  public void testDefaultMode_withArgument() {
    Arguments args = Arguments.parse(new String[]{algorithmsExampleFile.toString(), "10"});
    Arguments expected = new Arguments(RunMode.DEFAULT, algorithmsExampleFile, null, false, false, new String[]{"10"});
    assertEquals(expected, args);
  }

  @Test
  public void testDefaultMode_withFolder_withArgument() {
    Arguments args = Arguments.parse(new String[]{"-d", samplesFolder.toString(), algorithmsExampleFile.toString(), "20"});
    Arguments expected = new Arguments(RunMode.DEFAULT, algorithmsExampleFile, samplesFolder, false, false, new String[]{"20"});
    assertEquals(expected, args);
  }

  @Test
  public void testDefaultMode_withFolder_notADirectory() {
    String[] args1 = new String[]{"--sources-directory", simpleExampleFile.toString()};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1));
    assertEquals("Not a directory: " + simpleExampleFile.toAbsolutePath().normalize(), exception.getMessage());
    String[] args2 = new String[]{"-s", "-d", simpleExampleFile.toAbsolutePath().toString(), simpleExampleFile.toString()};
    exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2));
    assertEquals("Not a directory: " + simpleExampleFile.toAbsolutePath().normalize(), exception.getMessage());
  }

  @Test
  public void testDefaultMode_withFolder_mainNotADescendent() {
    String[] args = new String[]{"-d", samplesFolder.toString(), foreignJavaFile.toString()};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args));
    assertEquals("Main file must be located inside the sources directory or in any child directory.", exception.getMessage());
  }

  @Test
  public void testDefaultMode_notAJavaFile() {
    String[] args1 = new String[]{"-d", samplesFolder.toAbsolutePath().toString(), samplesFolder.resolve("notAJavaFile.txt").toString()};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1));
    assertEquals("Not a Java source file: " + samplesFolder.resolve("notAJavaFile.txt").toAbsolutePath().normalize(), exception.getMessage());
    String[] args2 = new String[]{"-d", samplesFolder.toString(), samplesFolder.toString()};
    exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2));
    assertEquals("Not a Java source file: " + samplesFolder.toAbsolutePath().normalize(), exception.getMessage());
    String[] args3 = new String[]{"-s", "-v", samplesFolder.toAbsolutePath().resolve("./../sample/files/shopping-list.xml").toString()};
    exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args3));
    assertEquals("Not a Java source file: " + shoppingListFile.toAbsolutePath().normalize(), exception.getMessage());
  }

  @Test
  public void testInstrumentOnly_missingArgument() {
    String[] args1 = new String[]{"-i"};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1));
    assertEquals("Exactly one argument required for the instrument-only run mode.", exception.getMessage());
    String[] args2 = new String[]{"--instrument-only"};
    exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2));
    assertEquals("Exactly one argument required for the instrument-only run mode.", exception.getMessage());
  }

  @Test
  public void testInstrumentOnly_tooManyArguments() {
    String[] args1 = new String[]{"-i", "filePathString", "additionalArg"};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1));
    assertEquals("Exactly one argument required for the instrument-only run mode.", exception.getMessage());
    String[] args2 = new String[]{"--instrument-only", "filePathString", "additionalArg"};
    exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2));
    assertEquals("Exactly one argument required for the instrument-only run mode.", exception.getMessage());
  }

  @Test
  public void testDefaultMode_withFolder_missingMainArgument() {
    String[] args = new String[]{"-d", samplesFolder.toString()};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args));
    assertEquals("No main file specified.", exception.getMessage());
  }

  @Test
  public void testDefaultMode_withFolder_missingDirectoryArgument() {
    String[] args = new String[]{"--sources-directory"};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args));
    assertEquals("No sources directory specified.", exception.getMessage());
  }

  @Test
  public void testReportOnly() {
    Arguments expected = new Arguments(RunMode.REPORT_ONLY, null, null, false, false, null);
    assertEquals(expected, Arguments.parse(new String[]{"-r"}));
  }

  @Test
  public void testReportOnly_unexpectedArgument() {
    String[] args = new String[]{"--generate-report", samplesFolder.toString()};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args));
    assertEquals("No arguments allowed for the report-only run mode.", exception.getMessage());
  }

  @Test
  public void testInstrumentOnly() {
    Arguments expected = new Arguments(RunMode.INSTRUMENT_ONLY, simpleExampleFile, null, false, false, null);
    assertEquals(expected, Arguments.parse(new String[]{"-i", simpleExampleFile.toString()}));
    assertEquals(expected, Arguments.parse(new String[]{"--instrument-only", simpleExampleFile.toString()}));
  }

  @Test
  public void testInstrumentOnly_folder() {
    Arguments expected = new Arguments(RunMode.INSTRUMENT_ONLY, samplesFolder, null, false, false, null);
    assertEquals(expected, Arguments.parse(new String[]{"-i", samplesFolder.toString()}));
    assertEquals(expected, Arguments.parse(new String[]{"--instrument-only", samplesFolder.toString()}));
  }

  @Test
  public void testFullArguments() {
    Arguments args = Arguments.parse(new String[]{
        "-v", "-s", "-d", samplesFolder.toString(), lambdaExampleFile.toString(), "arg1", "arg2", "arg3"});
    Arguments expected = new Arguments(
        RunMode.DEFAULT, lambdaExampleFile, samplesFolder, true, true, new String[]{"arg1", "arg2", "arg3"});
    assertEquals(expected, args);
  }

  @Test
  public void testExclusiveRunModes() {
    // instrument-only first
    String[] args1 = new String[]{"-i", "-r", samplesFolder.toString()};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1));
    assertEquals("Multiple run modes specified.", exception.getMessage());
    // report-only first
    String[] args2 = new String[]{"-v", "--generate-report", "-s", "--instrument-only", simpleExampleFile.toString()};
    exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args2));
    assertEquals("Multiple run modes specified.", exception.getMessage());
  }

  @Test
  public void testOnlyOptions() {
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(new String[]{"-s"}));
    assertEquals("No main file specified.", exception.getMessage());
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(new String[]{"--v"}));
    assertEquals("No main file specified.", exception.getMessage());
    assertThrows(IllegalArgumentException.class, () -> Arguments.parse(new String[]{"--verbose", "--synchronized"}));
    assertEquals("No main file specified.", exception.getMessage());
  }

  @Test
  public void testUnknownOption() {
    String[] args1 = new String[]{"-x", "argument"};
    var exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(args1));
    assertEquals("Unknown option: -x", exception.getMessage());
    String[] arg2 = new String[]{"-s", "-v", "--unknown"};
    exception = assertThrows(IllegalArgumentException.class, () -> Arguments.parse(arg2));
    assertEquals("Unknown option: --unknown", exception.getMessage());
  }

}
