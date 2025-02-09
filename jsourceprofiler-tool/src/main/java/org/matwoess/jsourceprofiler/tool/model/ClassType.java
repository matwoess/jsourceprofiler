package org.matwoess.jsourceprofiler.tool.model;

/**
 * This enum is used to represent the locality type for a class definition.
 * <p>
 * It does not distinguish between <code>class</code>, <code>interface</code>, <code>enum</code> or <code>record</code>.
 * Rather it differentiates between common top-level or nested classes and <code>anonymous</code> and <code>local</code> ones.
 * <p>
 * Used to correctly restore the previous block when exiting local or anonymous classes inside methods.
 */
public enum ClassType {
  /**
   * A default class, interface, enum or record.
   */
  CLASS,
  /**
   * An anonymous class.
   */
  ANONYMOUS,
  /**
   * A local class.
   */
  LOCAL
}
