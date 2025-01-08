package org.matwoess.jsourceprofiler.tool.profile;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.tool.model.Block;
import org.matwoess.jsourceprofiler.tool.model.CodeInsert;
import org.matwoess.jsourceprofiler.tool.model.CodeRegion;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The writer class for the annotated source code report file of a java file.
 */
public class ReportSourceWriter extends AbstractHtmlWriter {
  private final JavaFile javaFile;

  /**
   * Creates a new {@link ReportSourceWriter} object for the given java file.
   *
   * @param javaFile the file to write the report file for
   */
  public ReportSourceWriter(JavaFile javaFile) {
    this.javaFile = javaFile;
    this.title = javaFile.sourceFile.getFileName().toString();
    includeScripts = new String[]{
        "https://ajax.googleapis.com/ajax/libs/jquery/3.6.3/jquery.min.js",
        "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.10.0/highlight.min.js",
    };
    cssFiles = new String[]{
        "css/source.css",
        "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.10.0/styles/googlecode.min.css" // highlight.js theme
    };
    bodyScripts = new String[]{"js/syntax.js", "js/hits.js", "js/highlighter.js"};
  }

  /**
   * Generates the main content of the HTML document by calling {@link #preCodeElement}.
   */
  @Override
  public void body() {
    preCodeElement();
  }

  /**
   * Appends all code inside a pre/code block to the internal <code>content</code> StringBuilder.
   * <p>
   * First, the source code is read from the file.
   * Then, the code is annotated with spans for each code block and code region.
   * It is then post-processed to remove empty spans and spans with only whitespace.
   * Finally, the annotated code is converted to a table with line numbers and hit counts.
   */
  private void preCodeElement() {
    content.append("<pre>\n");
    content.append("<code class=\"language-java\">\n");
    try {
      String sourceCode = Files.readString(javaFile.sourceFile, StandardCharsets.ISO_8859_1);
      StringBuilder builder = new StringBuilder();
      int prevIdx = 0;
      List<CodeInsert> tagInserts = getTagInserts(sourceCode);
      for (CodeInsert tagInsert : tagInserts) {
        builder.append(ReportUtil.escapeHtmlTagCharacters(sourceCode.substring(prevIdx, tagInsert.chPos())));
        prevIdx = tagInsert.chPos();
        builder.append(tagInsert.code());
      }
      builder.append(sourceCode.substring(prevIdx));
      String annotatedCode = builder.toString();
      annotatedCode = postProcessAnnotatedCode(annotatedCode);
      String tabledCode = getCodeTable(annotatedCode);
      content.append(tabledCode);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    content.append("</code>\n");
    content.append("</pre>\n");
  }

  /**
   * Converts the annotated code to a table with line numbers and hit counts.
   *
   * @param annotatedCode the annotated code
   * @return the table-wrapped code
   */
  private String getCodeTable(String annotatedCode) {
    StringBuilder builder = new StringBuilder();
    builder.append("<table>\n");
    int lineNr = 1;
    String[] splitLines = annotatedCode.split("\n");
    for (String line : splitLines) {
      builder.append(String.format("<tr id=\"%s\">", lineNr));
      builder.append("<td class=\"code\">").append(line).append("</td>");
      builder.append("</tr>\n");
      lineNr++;
    }
    builder.append("</table>\n");
    return builder.toString();
  }

  private static final String spanPatternStr = "<span class=\"[^\"]*\" title=\"[^\"]*\">";
  private static final Pattern emptySpanPattern = Pattern.compile(spanPatternStr + "</span>", Pattern.MULTILINE);
  private static final Pattern blankStartSpanBeforeSpanPattern = Pattern.compile(
      "^%s(\\s+)</span>(%s)(.*)".formatted(spanPatternStr, spanPatternStr),
      Pattern.MULTILINE);

  /**
   * Removes empty spans and spans with only whitespace from the given annotated code.
   *
   * @param code the annotated code
   * @return the post-processed annotated code
   */
  private String postProcessAnnotatedCode(String code) {
    code = blankStartSpanBeforeSpanPattern.matcher(code).replaceAll("$2$1$3");
    code = emptySpanPattern.matcher(code).replaceAll("");
    return code;
  }

  /**
   * Retrieves all the code inserts to annotate the given source code.
   * <p>
   * A span is created at the beginning and end of each code block and its code region.
   * Each tag contains a class attribute with the block id and the region id.
   * See {@link #codeSpanAt} for more details.
   * <p>
   * Some optimization is done to not add 0-length spans.
   *
   * @param sourceCode the source code to retrieve the code inserts for
   * @return a list of code inserts
   */
  private List<CodeInsert> getTagInserts(String sourceCode) {
    char lf = '\n';
    List<CodeInsert> inserts = new ArrayList<>();
    inserts.add(new CodeInsert(0, "<span>"));
    for (Block block : javaFile.foundBlocks) {
      if (sourceCode.charAt(block.beg.pos()) != lf) { // optimization to not add 0-length region spans
        inserts.add(new CodeInsert(block.beg.pos(), "</span>"));
        inserts.add(new CodeInsert(block.beg.pos(), codeSpanAt(block.beg.pos())));
      }
      for (CodeRegion region : block.codeRegions) {
        if (sourceCode.charAt(region.beg.pos()) != lf) { // optimization to not add 0-length region spans
          inserts.add(new CodeInsert(region.beg.pos(), "</span>"));
          inserts.add(new CodeInsert(region.beg.pos(), codeSpanAt(region.beg.pos())));
        }
        if (sourceCode.charAt(region.end.pos()) != lf) { // optimization to not add 0-length region spans
          inserts.add(new CodeInsert(region.end.pos(), "</span>"));
          inserts.add(new CodeInsert(region.end.pos(), codeSpanAt(region.end.pos())));
        }
      }
      if (sourceCode.charAt(block.end.pos()) != lf) { // optimization to not add 0-length region spans
        inserts.add(new CodeInsert(block.end.pos(), "</span>"));
        inserts.add(new CodeInsert(block.end.pos(), codeSpanAt(block.end.pos())));
      }
    }
    // add spans for line endings and line beginnings in preparation for tabled code
    for (int index = sourceCode.indexOf(lf); index >= 0; index = sourceCode.indexOf(lf, index + 1)) {
      inserts.add(new CodeInsert(index, "</span>"));
      inserts.add(new CodeInsert(index + 1, codeSpanAt(index + 1)));
    }
    // add final closing end-tag
    inserts.add(new CodeInsert(sourceCode.length(), "</span>"));
    inserts.sort(Comparator.comparing(CodeInsert::chPos));
    return inserts;
  }

  /**
   * Returns the code span for the given character position.
   * <p>
   * Each span contains a class attribute with all the active block and the region ids.
   *
   * @param chPos the character position in the original source code file
   * @return the code span
   */
  private String codeSpanAt(int chPos) {
    List<Block> activeBlocks = getActiveBlocksAtCharPosition(chPos);
    if (activeBlocks.isEmpty()) {
      return "<span>";
    }
    Block lastBlock = activeBlocks.get(activeBlocks.size() - 1);
    CodeRegion region = null;
    for (CodeRegion r : lastBlock.codeRegions) {
      if (r.beg.pos() <= chPos && chPos < r.end.pos()) {
        region = r;
      }
    }
    return codeSpan(activeBlocks, lastBlock, region);

  }

  /**
   * Returns the code span for the given active blocks and code region.
   * <p>
   * The innermost block is used to determine the hit count.
   * If the code region is not null, the hit count is overridden with the region hit count.
   * <p>
   * In the title attribute, the hit count is shown for information on hover.
   *
   * @param activeBlocks the list of active blocks
   * @param block        the innermost active block
   * @param region       the active code region
   * @return the HTML span element
   */
  private String codeSpan(List<Block> activeBlocks, Block block, CodeRegion region) {
    long hits = (region != null) ? region.getHitCount() : block.hits;
    String coverageClass = hits > 0 ? "c" : "nc";
    title = ReportUtil.formatHitCount(hits) + " hit" + (hits == 1 ? "" : "s");
    String classes = "b " + (region != null ? "r " : "");
    classes += activeBlocks.stream().map(b -> "b" + b.id).collect(Collectors.joining(" "));
    if (region != null) {
      classes += " r" + activeBlocks.get(activeBlocks.size() - 1).id + "_" + region.id;
      if (!region.dependentBlocks.isEmpty()) {
        title += " ("
            + ReportUtil.formatHitCount(region.block.hits) + " - "
            + region.dependentBlocks.stream()
            .map(b -> ReportUtil.formatHitCount(b.hits))
            .collect(Collectors.joining(" - "))
            + ")";
        classes = getDependentBlockClasses(region) + " " + classes;
      }
    }
    return String.format("<span class=\"%s %s\" title=\"%s\">", coverageClass, classes, title);
  }

  /**
   * Returns the classes for all the dependent control breaks of the given code region.
   *
   * @param region the code region
   * @return the classes for all the dependent control breaks
   */
  private static String getDependentBlockClasses(CodeRegion region) {
    return region.dependentBlocks.stream().distinct().map(b -> "d" + b.id).collect(Collectors.joining(" "));
  }

  /**
   * Returns a list of all active blocks at the given character position.
   *
   * @param chPos the character position
   * @return the list of all active blocks
   */
  private List<Block> getActiveBlocksAtCharPosition(int chPos) {
    return javaFile.foundBlocks.stream()
        .filter(b -> b.beg.pos() <= chPos && chPos < b.end.pos() && b.blockType.hasCounter())
        .collect(Collectors.toList());
  }

  /**
   * Returns the file output path to write the file to.
   * The path is determined by calling {@link IO#getReportSourceFilePath} with the relative java file path.
   *
   * @return the file output path
   */
  @Override
  public Path getFileOutputPath() {
    return IO.getReportSourceFilePath(javaFile.relativePath);
  }
}