package org.matwoess.jsourceprofiler.tool.model;

import java.io.Serializable;

import static org.matwoess.jsourceprofiler.tool.model.BlockType.*;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.*;

/**
 * This class is used to represent a control flow break.
 *
 * @param kind  the kind of control break (one of {@link Kind})
 * @param label the target label of the control break, if any
 */
public record ControlBreak(Kind kind, String label) implements Serializable {

  /**
   * The kind of control flow break.
   */
  public enum Kind {
    /**
     * A break statement.
     */
    BREAK,
    /**
     * A continue statement.
     */
    CONTINUE,
    /**
     * A return statement.
     */
    RETURN,
    /**
     * A switch expression yield statement.
     */
    YIELD,
    /**
     * A throw statement.
     */
    THROW
  }

  /**
   * Decide when to stop propagating a control break at a given parent block.
   *
   * @param block the block to check
   * @return true if this kind of control flow break should be propagated only until the given block
   */
  public boolean stopPropagationAt(Block block) {
    if (label != null) return block.labels.contains(label);
    return switch (kind) {
      case BREAK -> block.blockType == LOOP || block.isSwitchStatementCase();
      case CONTINUE -> block.blockType == LOOP;
      case YIELD -> block.isSwitchExpressionCase();
      case RETURN -> block.blockType == METHOD || block.blockType == LAMBDA;
      case THROW -> block.blockType == METHOD || block.blockType == TRY;
    };
  }

  /**
   * Create a new control flow break from a keyword token value.
   *
   * @param tokenValue the keyword string of the control break
   * @return a new control break instance with a kind matching the token string
   */
  public static ControlBreak fromToken(String tokenValue) {
    Kind type = switch (tokenValue) {
      case "break" -> BREAK;
      case "continue" -> CONTINUE;
      case "return" -> RETURN;
      case "yield" -> YIELD;
      case "throw" -> THROW;
      default -> throw new RuntimeException("unknown control flow break '" + tokenValue + "'");
    };
    return new ControlBreak(type, null);
  }

  /**
   * Create a new control flow break from a keyword token value and a label.
   *
   * @param tokenValue the keyword of the control break
   * @param label      the target label of the control break
   * @return a new control break instance with the given label
   */
  public static ControlBreak fromTokenWithLabel(String tokenValue, String label) {
    assert tokenValue.equals("break") || tokenValue.equals("continue");
    Kind type = switch (tokenValue) {
      case "break" -> BREAK;
      case "continue" -> CONTINUE;
      default -> throw new RuntimeException("unknown control break '" + tokenValue + "'");
    };
    return new ControlBreak(type, label);
  }

  @Override
  public String toString() {
    return kind.name() + (label == null ? "" : " " + label);
  }
}
