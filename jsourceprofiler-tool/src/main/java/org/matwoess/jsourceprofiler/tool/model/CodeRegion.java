package org.matwoess.jsourceprofiler.tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for a group of statements, inside a block, sharing one hit-count value.
 * <p>
 * Used to correctly determine the hit-count for a line of code and for visualization in the report.
 * <p>
 * A list of dependent control break blocks is used
 * to correctly calculate the effective hit-count of the current region.
 * When a block is reentered after a control break block, we need to subtract the hits of this block
 * to calculate the real count of the new region.
 * This is because we do not have an actual counter after <code>break</code>s,
 * <code>continue</code>s and <code>return</code>s, etc.
 */
public class CodeRegion implements Serializable, Component {
  /**
   * The unique identifier of this region.
   */
  public int id;
  /**
   * The start position of the code region in the source file.
   */
  public CodePosition beg;
  /**
   * The end position of the code region in the source file.
   */
  public CodePosition end;
  /**
   * The block this region belongs to.
   */
  public Block block;
  /**
   * The list of dependent control break blocks, used to calculate the effective hit-count of the current region.
   */
  public final List<Block> dependentBlocks = new ArrayList<>();

  /**
   * Gets the effective hit-count of this region.
   *
   * @return the parent block's hits minus the sum of hits form all dependent control break blocks
   */
  public long getHitCount() {
    return block.hits - dependentBlocks.stream().mapToLong(b -> b.hits).sum();
  }

  @Override
  public String toString() {
    return String.format(
        "CodeRegion (beg=%s, end=%s, dependentBlocks=%s)",
        beg, end, Arrays.toString(dependentBlocks.toArray())
    );
  }
}
