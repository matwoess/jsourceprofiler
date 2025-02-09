package org.matwoess.jsourceprofiler.tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a java method.
 * <p>
 * Stores the containing class, the method name and the block representing the method body.
 */
public class Method implements Serializable, Component {
  /**
   * The name of the method.
   */
  public final String name;
  /**
   * The containing class this method belongs to.
   */
  private JClass parentClass;
  /**
   * The block representing the method body (if not abstract).
   */
  public Block methodBlock = null;

  /**
   * Creates a new Method with the given name.
   *
   * @param name the name of the method
   */
  public Method(String name) {
    this.name = name;
  }

  /**
   * Stores the given class as the parent class of this method.
   * <p>
   * Additionally, this method is added to the list of methods for the parent class.
   *
   * @param parentClass the parent class
   */
  public void setParentClass(JClass parentClass) {
    assert this.parentClass == null;
    this.parentClass = parentClass;
    parentClass.methods.add(this);
  }

  /**
   * Sets the given block as the method body.
   *
   * @param block the method body block
   */
  public void setMethodBlock(Block block) {
    assert methodBlock == null && block.blockType.isMethod();
    methodBlock = block;
  }

  /**
   * Returns the block representing the method body.
   *
   * @return the method body block
   */
  public Block getMethodBlock() {
    return methodBlock;
  }

  /**
   * Returns a list of all blocks in this method, including inner blocks recursively.
   * <p>
   * If this method is abstract, an empty list is returned.
   *
   * @return the list of all blocks in this method
   */
  public List<Block> getBlocksRecursive() {
    if (isAbstract()) return List.of();
    List<Block> blocks = new ArrayList<>();
    blocks.add(methodBlock);
    blocks.addAll(methodBlock.getInnerBlocksRecursive());
    return blocks;
  }

  /**
   * Whether this method is abstract.
   * @return true if this method has no body-block
   */
  public boolean isAbstract() {
    return methodBlock == null;
  }

  @Override
  public String toString() {
    return name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Method method = (Method) o;

    if (!name.equals(method.name)) return false;
    return parentClass.equals(method.parentClass);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + parentClass.hashCode();
    return result;
  }
}
