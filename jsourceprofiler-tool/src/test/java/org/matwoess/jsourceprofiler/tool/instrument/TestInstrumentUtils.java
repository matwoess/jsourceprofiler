package org.matwoess.jsourceprofiler.tool.instrument;

import org.matwoess.jsourceprofiler.common.OS;
import org.matwoess.jsourceprofiler.common.RunMode;
import org.matwoess.jsourceprofiler.tool.cli.Arguments;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestInstrumentUtils {

  private static Path createTempFileWithContent(String content) {
    try {
      List<String> fileContent = Arrays.asList(content.split(OS.getOS().lineSeparator()));
      Path tempFile = Files.createTempFile(null, null);
      Files.write(tempFile, fileContent);
      return tempFile;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Instrumenter analyzeStringContent(String content) {
    Path file = createTempFileWithContent(content);
    JavaFile javaFile = new JavaFile(file);
    Arguments args = new Arguments(RunMode.DEFAULT, null, null, false, true, null);
    Instrumenter instrumenter = new Instrumenter(new JavaFile[]{javaFile}, args);
    instrumenter.analyzeFiles();
    return instrumenter;
  }

  public static JavaFile parseJavaFile(String content) {
    Instrumenter instrumenter = analyzeStringContent(content);
    System.out.println(TestProgramBuilder.getBuilderCode(instrumenter.javaFiles[0]));
    return instrumenter.javaFiles[0];
  }

  public static void assertResultEquals(JavaFile expected, JavaFile actual) {
    assertEquals(expected.foundBlocks.size(), actual.foundBlocks.size());
    assertEquals(expected.topLevelClasses.size(), actual.topLevelClasses.size());
    assertIterableEquals(expected.topLevelClasses, actual.topLevelClasses);
    assertIterableEquals(expected.foundBlocks, actual.foundBlocks);
    assertEquals(expected.beginOfImports, actual.beginOfImports);
  }
}
