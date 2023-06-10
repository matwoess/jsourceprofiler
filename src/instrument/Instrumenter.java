package instrument;

import misc.CodeInsert;
import misc.IO;
import model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Instrumenter {
  JavaFile[] javaFiles;
  int blockCounter;

  public Instrumenter(JavaFile... javaFiles) {
    assert javaFiles.length > 0;
    this.javaFiles = javaFiles;
  }

  public void analyzeFiles() {
    for (JavaFile javaFile : javaFiles) {
      analyze(javaFile);
    }
  }

  void analyze(JavaFile javaFile) {
    System.out.println("Reading File: \"" + javaFile.sourceFile + "\"");
    Parser parser = new Parser(new Scanner(javaFile.sourceFile.toString()));
    parser.Parse();
    System.out.println();
    int errors = parser.errors.count;
    System.out.println("Errors found: " + errors);
    if (errors > 0) {
      throw new RuntimeException("Abort due to parse errors.");
    }
    javaFile.beginOfImports = parser.state.beginOfImports;
    javaFile.foundBlocks = parser.state.allBlocks;
    javaFile.foundClasses = parser.state.allClasses;
  }

  public void instrumentFiles() {
    blockCounter = 0;
    try {
      for (JavaFile javaFile : javaFiles) {
        instrument(javaFile);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    IO.copyAuxiliaryFiles();
    System.out.println();
    System.out.println("Total block found: " + blockCounter);
  }

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
    IO.createDirectoriesIfNotExists(javaFile.instrumentedFile);
    Files.writeString(javaFile.instrumentedFile, builder.toString());
  }

  List<CodeInsert> getCodeInserts(JavaFile javaFile) {
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(javaFile.beginOfImports, "import auxiliary.__Counter;"));
    for (Block block : javaFile.foundBlocks) {
      if (block.blockType.isNotYetSupported()) {
        blockCounter++;
        continue;
      }
      // insert order is important, in case of same CodeInsert char positions
      if (block.blockType.hasNoBraces()) {
        assert block.blockType != BlockType.METHOD;
        inserts.add(new CodeInsert(block.begPos, "{"));
      }
      inserts.add(new CodeInsert(block.getIncInsertPos(), String.format("__Counter.inc(%d);", blockCounter++)));
      if (block.blockType.hasNoBraces()) {
        inserts.add(new CodeInsert(block.endPos, "}"));
      }
    }
    inserts.sort(Comparator.comparing(CodeInsert::chPos));
    return inserts;
  }

  public void exportMetadata() {
    IO.exportMetadata(new IO.Metadata(blockCounter, javaFiles));
  }

}