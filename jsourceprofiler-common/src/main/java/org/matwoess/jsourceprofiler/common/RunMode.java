package org.matwoess.jsourceprofiler.common;

/**
 * The possible run modes of the tool.
 */
public enum RunMode {
  /**
   * Instrument, compile, execute the program and generate the report.
   */
  DEFAULT,
  /**
   * Only instrument a file or directory.
   */
  INSTRUMENT_ONLY,
  /**
   * Only generate the report from existing counts, metadata and source code files.
   */
  REPORT_ONLY;

  /**
   * {@return a description for the run mode}
   */
  @Override
  public String toString() {
    return switch (this) {
      case DEFAULT -> "Instrument, compile, run, report";
      case INSTRUMENT_ONLY -> "Instrument only";
      case REPORT_ONLY -> "Generate report only";
    };
  }
}