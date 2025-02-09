package org.matwoess.jsourceprofiler.tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to store information about a code block.
 * It is the most important class of the model package and used for correct instrumentation and report generation.
 */
public class Block implements Serializable, Component {
  /**
   * The unique identifier of this block.
   */
  public int id;
  /**
   * The class this block belongs to.
   */
  public JClass clazz;
  /**
   * The method this block belongs to.
   */
  private Method method;
  /**
   * The outer block surrounding this block.
   */
  private Block parentBlock;
  /**
   * The start position of the block in the source file.
   */
  public CodePosition beg;
  /**
   * The end position of the block in the source file.
   */
  public CodePosition end;
  /**
   * The type of the block (one of {@link BlockType}).
   */
  public BlockType blockType;
  /**
   * Whether this block is a single statement block (no curly braces).
   */
  public boolean isSingleStatement;

  /**
   * The list of nested blocks for this block. Empty by default.
   */
  public final List<Block> innerBlocks = new ArrayList<>();

  /**
   * The control flow break of this block. Null if this block is not ending with one.
   */
  public ControlBreak controlBreak = null;
  /**
   * The labels associated with this block. Empty by default.
   */
  public final List<String> labels = new ArrayList<>();
  /**
   * The code regions of this block. Should never be empty as long as {@link BlockType#hasCounter} is true.
   */
  public final List<CodeRegion> codeRegions = new ArrayList<>();

  /**
   * The offset for inserting the block's counter-statement. Important for constructor blocks.
   */
  public int incInsertOffset;

  /**
   * The number of times this block was hit during execution.
   * <p>
   * This information is added from the counts.dat file before report generation.
   */
  transient public long hits;
  /**
   * The list of inner blocks that contain a control break.
   * <p>
   * Used to determine minus blocks for new code regions.
   */
  public final transient List<Block> innerControlBreaks = new ArrayList<>();

  /**
   * Creates a new Block with the given type.
   *
   * @param type the type of the block (one of {@link BlockType})
   */
  public Block(BlockType type) {
    blockType = type;
  }

  /**
   * Registers the parent method object for this block.
   * <p>
   * If the block is a method block, it also registers this block as the method's method block.
   *
   * @param method the parent method object
   */
  public void setParentMethod(Method method) {
    assert this.method == null;
    if (method != null) {
      this.method = method;
      if (blockType.isMethod()) {
        method.setMethodBlock(this);
      }
    }
  }

  /**
   * Registers the parent class object for this block.
   * <p>
   * Also adds this block to the class' class blocks if it is not a method.
   *
   * @param clazz the parent class object
   */
  public void setParentClass(JClass clazz) {
    assert this.clazz == null;
    this.clazz = clazz;
    if (method == null && parentBlock == null) {
      clazz.classBlocks.add(this);
    }
  }

  /**
   * Registers the parent block object for this block.
   * <p>
   * Also adds this block to the parent block's inner blocks.
   * @param block the parent block object
   */
  public void setParentBlock(Block block) {
    if (block == null) return;
    parentBlock = block;
    parentBlock.innerBlocks.add(this);
  }

  /**
   * Add the given block to the list of inner control breaks.
   * @param controlBreakBlock the block containing the control break
   */
  public void registerInnerControlBreak(Block controlBreakBlock) {
    innerControlBreaks.add(controlBreakBlock);
  }

  /**
   * Insert the given region into the list of code regions.
   * @param region the region to add
   */
  public void addCodeRegion(CodeRegion region) {
    region.id = codeRegions.size();
    codeRegions.add(region);
  }

  /**
   * Returns the list of all inner blocks, below and excluding this one, recursively.
   * @return the list of all inner blocks
   */
  public List<Block> getInnerBlocksRecursive() {
    List<Block> blocks = new ArrayList<>();
    for (Block b : innerBlocks) {
      blocks.add(b);
      blocks.addAll(b.getInnerBlocksRecursive());
    }
    return blocks;
  }

  /**
   * Returns whether this block has no curly braces surrounding it.
   * @return whether it is a single statement or the block type has no braces
   */
  public boolean hasNoBraces() {
    return isSingleStatement || blockType.hasNoBraces();
  }

  /**
   * Returns whether this block is a switch <b>statement</b> case.
   * <p>
   * True if the block is a switch case and its parent block is a switch statement.
   * @return whether this block is a switch statement case
   */
  public boolean isSwitchStatementCase() {
    return blockType.isSwitchCase()
        && parentBlock != null
        && parentBlock.blockType == BlockType.SWITCH_STMT;
  }

  /**
   * Returns whether this block is a switch <b>expression</b> case.
   * <p>
   * True if the block is a switch case and its parent block is a switch expression.
   * @return whether this block is a switch expression case
   */
  public boolean isSwitchExpressionCase() {
    return blockType.isSwitchCase()
        && parentBlock != null
        && parentBlock.blockType == BlockType.SWITCH_EXPR;
  }

  /**
   * Returns the position where the increment statement should be inserted.
   * @return the block begin position plus the increment insert offset
   */
  public int getIncInsertPos() {
    return beg.pos() + incInsertOffset;
  }

  public String toString() {
    return String.format("%s%s%s: {%d[%s%s]-%s[%s]} (%s%s)%s%s%s",
        labels.isEmpty() ? "" : String.join(": ", labels) + ": ",
        clazz.name,
        method != null ? ("." + method.name) : "",
        beg.line(),
        beg.pos(),
        incInsertOffset != 0 ? "(+" + incInsertOffset + ")" : "",
        end != null ? end.line() : "?",
        end != null ? end.pos() : "?",
        blockType.toString(),
        isSingleStatement ? ", SS" : "",
        method == null ? " [class-level]" : "",
        controlBreak != null ? " [" + controlBreak + "]" : "",
        parentBlock == null ? "" : " (in " + parentBlock + ")"
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Block block = (Block) o;
    if (!Objects.equals(beg, block.beg)) return false;
    if (!Objects.equals(end, block.end)) return false;
    if (incInsertOffset != block.incInsertOffset) return false;
    if (!clazz.equals(block.clazz)) return false;
    if (!Objects.equals(method, block.method)) return false;
    if (!Objects.equals(controlBreak, block.controlBreak)) return false;
    if (!Objects.equals(parentBlock, block.parentBlock)) return false;
    return blockType == block.blockType;
  }

  @Override
  public int hashCode() {
    int result = clazz.hashCode();
    result = 31 * result + (method != null ? method.hashCode() : 0);
    result = 31 * result + beg.hashCode();
    result = 31 * result + end.hashCode();
    result = 31 * result + blockType.hashCode();
    result = 31 * result + incInsertOffset;
    result = 31 * result + (controlBreak != null ? controlBreak.hashCode() : 0);
    result = 31 * result + (parentBlock != null ? parentBlock.hashCode() : 0);
    return result;
  }
}
