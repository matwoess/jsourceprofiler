package org.matwoess.jsourceprofiler.tool.profile;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.tool.model.JClass;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;
import org.matwoess.jsourceprofiler.tool.model.Method;

import java.nio.file.Path;
import java.util.*;

/**
 * The writer class for the class index overview page.
 * This is the main page of the report.
 */
public class ReportClassIndexWriter extends AbstractHtmlWriter {
  private final JavaFile[] allJavaFiles;

  /**
   * Creates a new {@link ReportClassIndexWriter} object.
   *
   * @param allJavaFiles the list of all java files contained in the project
   */
  public ReportClassIndexWriter(JavaFile[] allJavaFiles) {
    this.allJavaFiles = allJavaFiles;
    title = "Class Overview";
    cssFiles = new String[]{"css/index.css"};
    includeScripts = new String[]{"https://ajax.googleapis.com/ajax/libs/jquery/3.6.3/jquery.min.js"};
    bodyScripts = new String[]{"js/sorter.js"};
  }

  /**
   * Generates the main content of the HTML document by calling {@link #sortedClassTable}.
   */
  @Override
  public void body() {
    sortedClassTable();
  }

  /**
   * Appends a table of all classes sorted by the number of method invocations to the internal <code>content</code>.
   */
  public void sortedClassTable() {
    Map<JClass, JavaFile> fileByClass = new HashMap<>();
    for (JavaFile jFile : allJavaFiles) {
      for (JClass clazz : jFile.topLevelClasses) {
        fileByClass.put(clazz, jFile);
      }
    }
    List<JClass> sortedClasses = Arrays.stream(allJavaFiles)
        .flatMap(f -> f.topLevelClasses.stream())
        .filter(c -> c.getMethodsRecursive().stream().anyMatch(m -> !m.isAbstract()))
        .sorted(Comparator.comparingLong(JClass::getAggregatedMethodBlockCounts).reversed())
        .toList();
    content.append("<table class=\"sortable\">\n")
        .append("<tr>\n")
        .append("<th>Class</th>\n")
        .append("<th class=\"metric desc\">Total Method Calls</th>\n")
        .append("<th class=\"metric\">Block Hit Max</th>\n")
        .append("<th class=\"metric\">Method Coverage</th>\n")
        .append("<th>Source File</th>\n")
        .append("</tr>\n");
    for (JClass clazz : sortedClasses) {
      JavaFile javaFile = fileByClass.get(clazz);
      Path methIdxHref = IO.getReportMethodIndexPath(clazz.name).getFileName();
      Path sourceFileHref = IO.getReportDir().relativize(IO.getReportSourceFilePath(javaFile.relativePath));
      ComponentCoverage methodCoverage = getMethodCoverage(clazz);
      long aggregatedMethodHitCounts = clazz.getAggregatedMethodBlockCounts();
      long blockHitMax = getBlockHitMax(clazz);
      content.append("<tr>\n")
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n", IO.normalize(methIdxHref), clazz.getName()))
          .append(String.format("<td class=\"metric\" data-total=\"%s\">%s</td>\n",
              aggregatedMethodHitCounts, ReportUtil.formatHitCount(aggregatedMethodHitCounts)))
          .append(String.format("<td class=\"metric\" data-total=\"%s\">%s</td>\n",
              blockHitMax, ReportUtil.formatHitCount(blockHitMax)))
          .append(String.format("<td class=\"metric coverage\" data-percentage=\"%s\" data-total=\"%s\">%s</td>\n",
              methodCoverage.percentage(), methodCoverage.total(), methodCoverage))
          .append(String.format("<td><a href=\"%s\">%s</a></td>\n",
              IO.normalize(sourceFileHref), javaFile.sourceFile.toFile().getName()))
          .append("</tr>\n");
    }
    content.append("</table>\n");
  }

  /**
   * Returns the method coverage of a class as a {@link ComponentCoverage} object.
   * Covered methods are those with at least one hit.
   * Abstract methods are excluded from the calculation.
   *
   * @param clazz the class to calculate the coverage for
   * @return the method coverage object
   */
  private ComponentCoverage getMethodCoverage(JClass clazz) {
    List<Method> methods = clazz.getMethodsRecursive().stream().filter(m -> !m.isAbstract()).toList();
    int coveredMethods = (int) methods.stream().filter(m -> m.getMethodBlock().hits > 0).count();
    return new ComponentCoverage(coveredMethods, methods.size());
  }

  /**
   * Returns the hit-count of the most frequent executed inner block inside a class and its methods.
   *
   * @param clazz the class to calculate the maximum for
   * @return the hit-count of the most often executed inner block inside a class and its methods
   */
  private long getBlockHitMax(JClass clazz) {
    return clazz.getBlocksRecursive().stream()
        .map(b -> b.hits)
        .max(Long::compareTo)
        .orElse(0L);
  }

  /**
   * Defines the output path for the generated HTML document as {@link IO#getReportIndexPath}.
   */
  @Override
  public Path getFileOutputPath() {
    return IO.getReportIndexPath();
  }
}
