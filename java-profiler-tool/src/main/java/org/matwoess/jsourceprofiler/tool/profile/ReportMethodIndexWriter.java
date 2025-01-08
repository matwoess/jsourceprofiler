package org.matwoess.jsourceprofiler.tool.profile;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.tool.model.Block;
import org.matwoess.jsourceprofiler.tool.model.JClass;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;
import org.matwoess.jsourceprofiler.tool.model.Method;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * The writer class for the method index overview page of a java class.
 * This page lists all methods of a class sorted by the number of invocations.
 */
public class ReportMethodIndexWriter extends AbstractHtmlWriter {
  private final JClass clazz;
  private final Path reportSourceFile;

  /**
   * Creates a new {@link ReportMethodIndexWriter} object.
   *
   * @param clazz    the java class to write the method index for
   * @param javaFile the java file containing the class
   */
  public ReportMethodIndexWriter(JClass clazz, JavaFile javaFile) {
    this.clazz = clazz;
    this.reportSourceFile = IO.getReportSourceFilePath(javaFile.relativePath);
    title = "Methods in " + clazz.getFullName();
    cssFiles = new String[]{"css/index.css"};
    includeScripts = new String[]{"https://ajax.googleapis.com/ajax/libs/jquery/3.6.3/jquery.min.js"};
    bodyScripts = new String[]{"js/sorter.js"};
  }

  /**
   * Generates the main content of the HTML document by calling {@link #sortedMethodTable}.
   */
  @Override
  public void body() {
    sortedMethodTable();
  }

  /**
   * Appends a table of all methods sorted by the number of invocations to the internal <code>content</code>.
   */
  public void sortedMethodTable() {
    List<Method> sortedMethods = clazz.getMethodsRecursive().stream()
        .filter(method -> !method.isAbstract())
        .sorted(Comparator.comparingLong((Method m) -> m.getMethodBlock().hits).reversed())
        .toList();
    content.append("<table class=\"sortable\">\n")
        .append("<tr>\n")
        .append("<th>Method</th>\n")
        .append("<th class=\"metric desc\">Invocations</th>\n")
        .append("<th class=\"metric\">Block Hit Max</th>\n")
        .append("<th class=\"metric\">Code Block Coverage</th>\n")
        .append("</tr>\n");
    Path sourceFileHref = IO.getReportDir().relativize(reportSourceFile);
    for (Method meth : sortedMethods) {
      Block methBlock = meth.getMethodBlock();
      String lineNrRef = IO.normalize(sourceFileHref) + "#" + methBlock.beg.line();
      String methName = (methBlock.clazz != clazz)
          ? methBlock.clazz.getName() + "::" + meth.name
          : meth.name;
      ComponentCoverage blockCoverage = getBlockCoverage(meth);
      long methodHitCount = methBlock.hits;
      long blockHitMax = getBlockHitMax(meth);
      content.append("<tr>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", lineNrRef, methName))
          .append(String.format("<td class=\"metric\" data-total=\"%s\">%s</td>\n",
              methodHitCount, ReportUtil.formatHitCount(methodHitCount)))
          .append(String.format("<td class=\"metric\" data-total=\"%s\">%s</td>\n",
              blockHitMax, ReportUtil.formatHitCount(blockHitMax)))
          .append(String.format("<td class=\"metric coverage\" data-percentage=\"%s\" data-total=\"%s\">%s</td>\n",
              blockCoverage.percentage(), blockCoverage.total(), blockCoverage))
          .append("</tr>\n");
    }
    content.append("</table>\n");
  }

  /**
   * Returns the block coverage of a method as a {@link ComponentCoverage} object.
   * Covered blocks are those with at least one hit.
   *
   * @param method the method to calculate the coverage for
   * @return the block coverage object
   */
  private ComponentCoverage getBlockCoverage(Method method) {
    List<Block> blocks = method.getBlocksRecursive().stream().filter(b -> b.blockType.hasCounter()).toList();
    int coveredBlocks = (int) blocks.stream().filter(b -> b.hits > 0).count();
    return new ComponentCoverage(coveredBlocks, blocks.size());
  }

  /**
   * Returns the hit-count of the most frequent executed inner block inside a method.
   *
   * @param method the method to calculate the maximum for
   * @return the hit-count of the most often executed inner block
   */
  private long getBlockHitMax(Method method) {
    return method.getBlocksRecursive().stream()
        .map(b -> b.hits)
        .max(Long::compareTo)
        .orElse(0L);
  }

  /**
   * Returns the path to the HTML file
   * to write to by calling {@link IO#getReportMethodIndexPath(String)} for the current class name.
   *
   * @return the path to the HTML file to write the method index to
   */
  @Override
  public Path getFileOutputPath() {
    return IO.getReportMethodIndexPath(clazz.name);
  }
}
