package org.matwoess.jsourceprofiler.tool.model;

/**
 * This enum is used to represent the type for a code block.
 * <p>
 * Important to distinguish between different types of blocks for correct instrumentation and report generation.
 */
public enum BlockType {
  BLOCK, METHOD, CONSTRUCTOR, STATIC, LOOP, TRY, LAMBDA, SWITCH_STMT, SWITCH_EXPR, COLON_CASE, ARROW_CASE;

  public String toString() {
    String prefix = switch (this) {
      case BLOCK -> "";
      case METHOD -> "method ";
      case CONSTRUCTOR -> "constructor ";
      case STATIC -> "static ";
      case LOOP -> "loop ";
      case TRY -> "try ";
      case LAMBDA -> "lambda ";
      case SWITCH_STMT -> "switch statement ";
      case SWITCH_EXPR -> "switch expression ";
      case COLON_CASE -> "colon case ";
      case ARROW_CASE -> "arrow case ";
    };
    return prefix + "block";
  }

  /**
   * Returns whether this block is a switch statement or switch expression body.
   *
   * @return true if the type is SWITCH_STMT or SWITCH_EXPR
   */
  public boolean isSwitchBody() {
    return this == SWITCH_STMT || this == SWITCH_EXPR;
  }

  /**
   * Returns whether this block is a switch case.
   *
   * @return true if the type is COLON_CASE or ARROW_CASE
   */
  public boolean isSwitchCase() {
    return this == COLON_CASE || this == ARROW_CASE;
  }

  /**
   * Returns whether this block is a method or constructor method.
   *
   * @return true if the type is METHOD or CONSTRUCTOR
   */
  public boolean isMethod() {
    return this == METHOD || this == CONSTRUCTOR;
  }

  /**
   * Returns whether this block will be instrumented with a counter.
   *
   * @return true if it is not a switch statement/expression body
   */
  public boolean hasCounter() {
    return !isSwitchBody();
  }

  /**
   * Returns whether this block has no opening and closing braces.
   *
   * @return true if the type is COLON_CASE
   */
  public boolean hasNoBraces() {
    return this == COLON_CASE;
  }
}
