package org.matwoess.jsourceprofiler.tool.instrument;

import org.matwoess.jsourceprofiler.tool.model.*;

import java.util.List;

/**
 * This class is used to log the parsing process.
 * <p>
 * It is used to print the current entering or exiting of a program component to the console.
 * It also provides methods to log special events (e.g., finding a control flow break).
 */
public class Logger {
  private static final String GREEN = "\u001B[32m";
  private static final String RED = "\u001B[31m";
  private static final String BRIGHT = "\u001B[97m";
  private static final String RESET = "\u001B[0m";

  private final Parser parser;
  private static int indent = 1;
  /**
   * Whether the logger should print log messages.
   */
  public boolean active;

  /**
   * Creates a new Logger for the given parser.
   *
   * @param p the parser object to log for
   */
  public Logger(Parser p) {
    parser = p;
  }

  /**
   * Prints the given log message to the console.
   * <p>
   * Only prints the message if the logger is active.
   *
   * @param logMessage the log message to print
   */
  public void log(String logMessage) {
    if (!active) return;
    System.out.printf("%s%3d:%s%-" + indent + "s%s%n", BRIGHT, parser.t.line, RESET, "", logMessage);
  }

  /**
   * Prints the given log message to the console.
   * <p>
   * Only prints the message if the logger is active.
   *
   * @param formatString the format string of the log message to print
   * @param values       the values to insert into the format string
   */
  public void log(String formatString, Object... values) {
    log(String.format(formatString, values));
  }

  /**
   * Print a log message for the entering of the given component.
   * <p>
   * Also increases the indentation.
   *
   * @param comp the component to enter
   */
  void enter(Component comp) {
    log(describe(comp, false) + GREEN + " -->" + RESET);
    indent += 2;
  }

  /**
   * Print a log message for the exiting of the given component.
   * <p>
   * Also decreases the indentation.
   * @param comp the component to leave
   */
  void leave(Component comp) {
    indent -= 2;
    log(describe(comp, true) + RED + " <--" + RESET);
  }

  /**
   * Returns a string describing the given component.
   * @param comp the component to describe
   * @param leave whether the component is entered or left
   * @return a string describing the given component
   */
  private String describe(Component comp, boolean leave) {
    if (comp instanceof JClass clazz) return "class <" + clazz.getFullName() + ">";
    if (comp instanceof Method meth) return meth + "()";
    if (comp instanceof Block block) {
      return String.format(
          "%s%s%s [%d]%s",
          block.labels.isEmpty() ? "" : String.join(": ", block.labels) + ": ",
          block.blockType,
          block.isSingleStatement ? ", SS" : "",
          leave ? block.end.pos() : block.beg.pos(),
          block.controlBreak == null ? "" : " (" + block.controlBreak + ")"
      );
    }
    if (comp instanceof CodeRegion region) {
      List<String> minusBlockLineNrs = region.dependentBlocks.stream()
          .map(b -> String.valueOf(b.beg.line()))
          .toList();
      return String.format(
          "region [%d]%s",
          leave ? region.end.pos() : region.beg.pos(),
          minusBlockLineNrs.isEmpty() ? "" : " -(" + String.join(",", minusBlockLineNrs) + ")"
      );
    }
    throw new RuntimeException("unknown component type: " + comp.getClass());
  }
}
