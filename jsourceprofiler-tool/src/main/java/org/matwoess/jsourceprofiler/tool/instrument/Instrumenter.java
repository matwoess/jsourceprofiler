package org.matwoess.jsourceprofiler.tool.instrument;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.tool.cli.Arguments;
import org.matwoess.jsourceprofiler.tool.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.THROW;

/**
 * This class is used to instrument the source files of a Java project.
 * <p>
 * It uses the {@link Parser} to parse the source files and find all code blocks.
 * <p>
 * It then inserts code to increment the counter for each code block.
 * <p>
 * Finally, it writes the instrumented source files to the instrumented directory.
 */
public class Instrumenter {
  JavaFile[] javaFiles;
  int blockCounter;
  public String incRefAdd;
  boolean verboseOutput;

  /**
   * Creates a new Instrumenter with the given source files.
   *
   * @param javaFiles the source files to instrument
   * @param toolArgs the tool command line arguments record
   */
  public Instrumenter(JavaFile[] javaFiles, Arguments toolArgs) {
    assert javaFiles.length > 0;
    this.verboseOutput = toolArgs.verboseOutput();
    incRefAdd = (toolArgs.syncCounters()) ? "Sync" : "";
    this.javaFiles = javaFiles;
  }

  /**
   * Parses all source files and find all code blocks.
   */
  public void analyzeFiles() {
    Arrays.stream(javaFiles).forEach(this::analyze);
  }

  /**
   * Parses the given source file and finds all code blocks.
   * <p>
   * The found code blocks and additional metadata are stored in the given JavaFile object.
   *
   * @param javaFile the source file to parse
   */
  void analyze(JavaFile javaFile) {
    System.out.println("Parsing file: \"" + javaFile.sourceFile + "\"");
    Parser parser = new Parser(new Scanner(javaFile.sourceFile.toString()));
    parser.state.logger.active = verboseOutput;
    parser.Parse();
    int errors = parser.errors.count;
    if (errors > 0) {
      throw new RuntimeException("Abort due to parse errors.");
    }
    javaFile.packageName = parser.state.packageName;
    javaFile.beginOfImports = parser.state.beginOfImports;
    javaFile.foundBlocks = parser.state.allBlocks;
    javaFile.topLevelClasses = parser.state.topLevelClasses;
  }

  /**
   * Instruments all source files.
   * <p>
   * The instrumented copy of each source file is then written to the instrumented directory.
   */
  public void instrumentFiles() {
    IO.clearDirectoryContents(IO.getInstrumentDir());
    blockCounter = 0;
    try {
      for (JavaFile javaFile : javaFiles) {
        instrument(javaFile);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    copyAuxiliaryFiles();
    System.out.println();
    List<JClass> allClasses = Arrays.stream(javaFiles).flatMap(jFile -> jFile.getClassesRecursive().stream()).toList();
    System.out.println("Total classes found: " + allClasses.size());
    List<Method> allMethods = allClasses.stream().flatMap(cls -> cls.methods.stream()).toList();
    System.out.println("Total methods found: " + allMethods.size());
    System.out.println("Total code block found: " + blockCounter);
  }

  /**
   * Instruments the given source file and writes the instrumented copy to the instrumented directory.
   *
   * @param javaFile the source file to instrument
   * @throws IOException if the source file or instrumented file cannot be read or written
   */
  void instrument(JavaFile javaFile) throws IOException {
    List<CodeInsert> codeInserts = getCodeInserts(javaFile);
    String fileContent = Files.readString(javaFile.sourceFile, StandardCharsets.ISO_8859_1);
    StringBuilder builder = new StringBuilder();
    int prevIdx = 0;
    for (CodeInsert codeInsert : codeInserts) {
      builder.append(fileContent, prevIdx, codeInsert.chPos());
      prevIdx = codeInsert.chPos();
      builder.append(codeInsert.code());
    }
    builder.append(fileContent.substring(prevIdx));
    Path instrumentedFilePath = IO.getInstrumentedFilePath(javaFile.relativePath);
    IO.createDirectoriesIfNotExists(instrumentedFilePath);
    Files.writeString(instrumentedFilePath, builder.toString());
  }

  /**
   * Returns a list of <code>CodeInsert</code> objects
   * that represent the code to be inserted into the given source file.
   *
   * @param javaFile the source file to instrument
   * @return a list of CodeInsert objects that represent the code to be inserted into the given source file
   */
  List<CodeInsert> getCodeInserts(JavaFile javaFile) {
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(javaFile.beginOfImports, "import auxiliary.__Counter;"));
    for (Block block : javaFile.foundBlocks) {
      if (block.blockType.isSwitchBody()) {
        continue;
      }
      // insert order is important, in case of same CodeInsert char positions
      if (block.isSingleStatement && block.blockType != BlockType.LAMBDA) {
        assert block.blockType != BlockType.METHOD;
        inserts.add(new CodeInsert(block.beg.pos(), "{"));
      }
      if (block.isSingleStatement && block.blockType == BlockType.LAMBDA) {
        inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.incLambda%s(%d, () -> ", incRefAdd, blockCounter++)));
        inserts.add(new CodeInsert(block.end.pos(), ")"));
      } else {
        inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.inc%s(%d);", incRefAdd, blockCounter++)));
      }
      if (block.isSingleStatement && block.isSwitchExpressionCase() && (block.controlBreak == null || block.controlBreak.kind() != THROW)) {
        inserts.add(new CodeInsert(block.getIncInsertPos(), "yield "));
      }
      if (block.isSingleStatement && block.blockType != BlockType.LAMBDA) {
        inserts.add(new CodeInsert(block.end.pos(), "}"));
      }
    }
    inserts.sort(Comparator.comparing(CodeInsert::chPos));
    return inserts;
  }

  /**
   * Exports the metadata file.
   */
  public void exportMetadata() {
    new Metadata(blockCounter, javaFiles).exportMetadata(IO.getMetadataPath());
  }

  /**
   * Copies the auxiliary files into the auxiliary directory.
   * <p>
   * Specifically, it copies the <code>__Counter.class</code> file to the auxiliary directory.
   */
  public static void copyAuxiliaryFiles() {
    IO.copyResource(Instrumenter.class, "auxiliary/__Counter.class", IO.getAuxiliaryCounterInstrumentPath());
  }
}