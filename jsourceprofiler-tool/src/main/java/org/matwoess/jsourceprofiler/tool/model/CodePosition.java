package org.matwoess.jsourceprofiler.tool.model;

import java.io.Serializable;

/**
 * A record class to store both the line and a character position in a unified way
 * @param line the line number
 * @param pos the character position
 */
public record CodePosition(int line, int pos) implements Serializable, Comparable<CodePosition> {
  @Override
  public int compareTo(CodePosition other) {
    int lineComparison = Integer.compare(this.line, other.line);
    if (lineComparison != 0) return lineComparison;
    return Integer.compare(this.pos, other.pos);
  }

  @Override
  public String toString() {
    return "[l=%d,p=%d]".formatted(line, pos);
  }
}
