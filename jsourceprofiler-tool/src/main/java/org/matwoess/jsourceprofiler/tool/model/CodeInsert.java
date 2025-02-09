package org.matwoess.jsourceprofiler.tool.model;

/**
 * This class is used to represent a code insertion.
 * <p>
 * It contains the character position and the code to insert.
 * <p>
 * Used during both instrumentation and report generation.
 *
 * @param chPos the character position index in the source file
 * @param code  the code to insert at this position
 */
public record CodeInsert(int chPos, String code) {
}
