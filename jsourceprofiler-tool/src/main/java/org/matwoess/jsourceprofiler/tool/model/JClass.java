package org.matwoess.jsourceprofiler.tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to store information about a java class.
 * <p>
 * Contains information about its name, the package, the parent class and the inner classes.
 * <p>
 * It also contains a list of methods and a list of class-level blocks.
 */
public class JClass implements Serializable, Component {
  /**
   * The name of the class.
   */
  public String name;
  /**
   * The type of the class (one of {@link ClassType}).
   */
  public ClassType classType;
  /**
   * The package name of the class (if any).
   */
  public String packageName;
  /**
   * The parent class of this class (if nested).
   */
  public JClass parentClass;
  /**
   * The list of nested classes inside this class.
   */
  public final List<JClass> innerClasses = new ArrayList<>();
  /**
   * The list of methods in this class.
   */
  public final List<Method> methods = new ArrayList<>();
  /**
   * The list of class-level blocks inside this class.
   */
  public final List<Block> classBlocks = new ArrayList<>();

  /**
   * Creates a new JClass with the given name and class type.
   * @param name the name of the class
   * @param type the class type (one of {@link ClassType})
   */
  public JClass(String name, ClassType type) {
    this.name = name;
    this.classType = type;
  }

  /**
   * Registers this class as a nested inner class of the given parent class.
   * <p>
   * If the name is empty, it will be assigned a number based on the number of already registered anonymous classes.
   * @param parentClass the containing parent class
   */
  public void setParentClass(JClass parentClass) {
    if (parentClass == null) return;
    if (name == null) {
      long nextAnonymousClass = parentClass.innerClasses.stream().filter(c -> c.classType == ClassType.ANONYMOUS).count() + 1;
      this.name = String.valueOf(nextAnonymousClass);
    }
    this.parentClass = parentClass;
    parentClass.innerClasses.add(this);
  }

  /**
   * Returns the name of this class, including the parent class name if it is an inner class,
   * separated by a <code>$</code>.
   * @return the name of this class
   */
  public String getName() {
    if (parentClass != null) {
      return parentClass.getName() + "$" + name;
    } else {
      return name;
    }
  }

  /**
   * Returns the full name of this class, including the package name.
   * @return the full name of this class
   */
  public String getFullName() {
    if (packageName == null) {
      return getName();
    } else {
      return packageName + "." + getName();
    }
  }

  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * Returns a list of all inner classes recursively.
   * @return the list of child classes
   */
  public List<JClass> getClassesRecursive() {
    List<JClass> allClasses = new ArrayList<>(innerClasses);
    for (JClass clazz : innerClasses) {
      allClasses.addAll(clazz.getClassesRecursive());
    }
    return allClasses;
  }

  /**
   * Returns a list of all methods in this class and all inner classes recursively.
   * @return all methods in this class and all inner classes
   */
  public List<Method> getMethodsRecursive() {
    List<Method> allMethods = new ArrayList<>(methods);
    for (JClass clazz : innerClasses) {
      allMethods.addAll(clazz.getMethodsRecursive());
    }
    return allMethods;
  }

  /**
   * Returns the sum of hits for all method blocks in this class and all inner classes recursively.
   * @return the total number of hits for any method is this top-level class
   */
  public long getAggregatedMethodBlockCounts() {
    return getMethodsRecursive().stream()
        .filter(m -> !m.isAbstract())
        .map(Method::getMethodBlock)
        .mapToLong(b -> b.hits)
        .sum();
  }

  /**
   * Returns a list of all blocks in this class and all inner classes recursively.
   * @return the list of all blocks contained inside this class
   */
  public List<Block> getBlocksRecursive() {
    List<Block> allBlocks = new ArrayList<>();
    allBlocks.addAll(classBlocks);
    allBlocks.addAll(classBlocks.stream().flatMap(block -> block.getInnerBlocksRecursive().stream()).toList());
    allBlocks.addAll(methods.stream().flatMap(method -> method.getBlocksRecursive().stream()).toList());
    for (JClass clazz : innerClasses) {
      allBlocks.addAll(clazz.getBlocksRecursive());
    }
    return allBlocks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JClass aClass = (JClass) o;
    if (!Objects.equals(name, aClass.name)) return false;
    if (classType != aClass.classType) return false;
    if (!Objects.equals(packageName, aClass.packageName)) return false;
    return Objects.equals(parentClass, aClass.parentClass);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + classType.hashCode();
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    result = 31 * result + (parentClass != null ? parentClass.hashCode() : 0);
    return result;
  }
}
