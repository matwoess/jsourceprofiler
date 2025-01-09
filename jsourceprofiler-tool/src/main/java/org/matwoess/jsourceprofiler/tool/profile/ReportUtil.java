package org.matwoess.jsourceprofiler.tool.profile;

/**
 * A utility class for common report generation helper methods.
 */
public class ReportUtil {

  /**
   * Format a number as a string with commas as thousands separator.
   *
   * @param number the number to be formatted
   * @return the formatted number string
   */
  static String formatHitCount(long number) {
    return String.format("%,d", number);
  }

  /**
   * Escapes the HTML tag characters <code>&lt;</code> and <code>&gt;</code> in the given code.
   *
   * @param code the code to escape
   * @return the escaped code
   */
  static String escapeHtmlTagCharacters(String code) {
    return code.replace("<", "&lt;").replace(">", "&gt;");
  }
}
