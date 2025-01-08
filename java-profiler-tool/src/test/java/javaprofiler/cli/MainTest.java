package javaprofiler.cli;

import org.junit.jupiter.api.Test;
import org.matwoess.jsourceprofiler.common.*;
import org.matwoess.jsourceprofiler.tool.cli.Main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
  final Path samplesFolder = Path.of("..", "sample");
  final Path simpleExampleFile = samplesFolder.resolve("Simple.java");
  final Path lambdaExampleFile = samplesFolder.resolve("Lambdas.java");
  final Path algorithmsExampleFile = samplesFolder.resolve("Algorithms.java");

  @Test
  public void testShowUsage_noError() {
    Main.main(new String[]{"-h"});
    Main.main(new String[]{"--help"});
  }

  @Test
  public void testNoArguments_errorAndHint() throws Exception {
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    try {
      System.setOut(new PrintStream(outContent));
      System.setErr(new PrintStream(outContent));
      int statusCode = catchSystemExit(() -> Main.main(new String[0]));
      assertEquals(1, statusCode);
      String newline = OS.getOS().lineSeparator();
      assertEquals("No arguments specified." + newline + "Use -h for help." + newline, outContent.toString());
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
    }
  }

  @Test
  public void testDefaultMode() {
    Main.main(new String[]{simpleExampleFile.toString()});
  }

  @Test
  public void testDefaultMode_withFolder() {
    Main.main(new String[]{"-d", samplesFolder.toString(), simpleExampleFile.toString()});
  }

  @Test
  public void testDefaultMode_withArgument() {
    Main.main(new String[]{algorithmsExampleFile.toString(), "10"});
  }

  @Test
  public void testDefaultMode_withFolder_withArgument() {
    Main.main(new String[]{"-d", samplesFolder.toString(), algorithmsExampleFile.toString(), "20"});
  }

  @Test
  public void testDefaultMode_withFolder_withArgument_absolutePaths() {
    Main.main(new String[]{"-d", samplesFolder.toAbsolutePath().toString(), algorithmsExampleFile.toAbsolutePath().toString(), "20"});
  }

  @Test
  public void testDefaultMode_withFolder_mismatchingPathTypes() {
    Main.main(new String[]{"-d", samplesFolder.toAbsolutePath().toString(), algorithmsExampleFile.toString(), "20"});
    Main.main(new String[]{"-d", samplesFolder.toString(), lambdaExampleFile.toAbsolutePath().toString()});
  }

  @Test
  public void testInstrumentOnly() {
    Main.main(new String[]{"-i", simpleExampleFile.toString()});
    Main.main(new String[]{"--instrument-only", simpleExampleFile.toString()});
  }

  @Test
  public void testInstrumentOnly_withFolder() {
    Main.main(new String[]{"-i", samplesFolder.toString()});
    Main.main(new String[]{"--instrument-only", samplesFolder.toString()});
  }

  @Test
  public void testInstrumentOnly_manualCompile_thenReportOnly() {
    Main.main(new String[]{"-i", samplesFolder.toString()});
    String filename = simpleExampleFile.getFileName().toString();
    Path instrDir = IO.getInstrumentDir();
    int exitCode = Util.runCommand(new JCompilerCommandBuilder()
        .setClassPath(instrDir)
        .setDirectory(instrDir)
        .addSourceFile(IO.getInstrumentDir().resolve(filename))
        .build());
    assertEquals(0, exitCode);
    exitCode = Util.runCommand(new JavaCommandBuilder()
        .setClassPath(instrDir)
        .setMainClass(filename.replace(".java", ""))
        .build());
    assertEquals(0, exitCode);
    Main.main(new String[]{"-r"});
    assertTrue(IO.getReportIndexSymLinkPath().toFile().exists());
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void testReportOnly_missingMetadataOrCounts() {
    File metadataFile = IO.getMetadataPath().toFile();
    if (metadataFile.exists()) {
      metadataFile.delete();
    }
    File countsFile = IO.getCountsPath().toFile();
    if (countsFile.exists()) {
      countsFile.delete();
    }
    RuntimeException ex = assertThrows(RuntimeException.class, () -> Main.main(new String[]{"-r"}));
    assertTrue(ex.getMessage().contains("metadata.dat"));
    Main.main(new String[]{"-i", simpleExampleFile.toString()});
    ex = assertThrows(RuntimeException.class, () -> Main.main(new String[]{"-r"}));
    assertTrue(ex.getMessage().contains("counts.dat"));
  }

  @Test
  public void testDefaultMode_synchronized() {
    Main.main(new String[]{"-s", lambdaExampleFile.toString()});
    Main.main(new String[]{"-s", "-d", samplesFolder.toString(), lambdaExampleFile.toString()});
  }

  @Test
  public void testDefaultMode_verbose() {
    Main.main(new String[]{"-v", lambdaExampleFile.toString()});
    Main.main(new String[]{"-v", "-d", samplesFolder.toString(), simpleExampleFile.toString()});
  }

  @Test
  public void testInstrumentOnly_synchronized() throws IOException {
    Main.main(new String[]{"-s", "-i", simpleExampleFile.toString()});
    String instrumentedContent = Files.readString(IO.getInstrumentDir().resolve(simpleExampleFile.getFileName()));
    assertTrue(instrumentedContent.contains("incSync("));
    assertFalse(instrumentedContent.contains("inc("));
    Main.main(new String[]{"--synchronized", "--verbose", "-i", lambdaExampleFile.toString()});
    instrumentedContent = Files.readString(IO.getInstrumentDir().resolve(lambdaExampleFile.getFileName()));
    assertTrue(instrumentedContent.contains("incSync("));
    assertTrue(instrumentedContent.contains("incLambdaSync("));
    assertFalse(instrumentedContent.contains("inc("));
    assertFalse(instrumentedContent.contains("incLambda("));
  }

}
