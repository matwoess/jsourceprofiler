package org.matwoess.jsourceprofiler.common;

/**
 * The possible run modes of the tool.
 */
public enum RunMode {
  DEFAULT, INSTRUMENT_ONLY, REPORT_ONLY;

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