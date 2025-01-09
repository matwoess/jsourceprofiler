package org.matwoess.jsourceprofiler.tool.profile;

import java.text.DecimalFormat;

/**
 * A simple record class to store coverage information.
 * Used primarily for generation of class and method index files in the HTML report.
 *
 * @param covered the number of covered items
 * @param total   the total number of items
 */
record ComponentCoverage(int covered, int total) {
  /**
   * Formatter for index percentage values with zero or one decimal places.
   */
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

  /**
   * Calculates the percentage of covered items.
   *
   * @return the ratio of covered items to total items in percent
   */
  public float percentage() {
    return (float) covered / total * 100;
  }

  @Override
  public String toString() {
    return String.format("(%d/%d) %s%%", covered, total, DECIMAL_FORMAT.format(percentage()));
  }
}
