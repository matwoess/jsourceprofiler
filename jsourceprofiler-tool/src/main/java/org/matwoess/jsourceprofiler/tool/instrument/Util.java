package org.matwoess.jsourceprofiler.tool.instrument;

import org.matwoess.jsourceprofiler.tool.model.BlockType;
import org.matwoess.jsourceprofiler.tool.model.CodePosition;

/**
 * This class contains utility methods for the {@link ParserState}.
 */
public class Util {
  /**
   * Returns the start character position of the given token.
   *
   * @param token the token to get the start position of
   * @return the start position of the given token
   */
  static int startOfToken(Token token) {
    return token.charPos;
  }

  /**
   * Returns the end character position of the given token.
   *
   * @param token the token to get the end position of
   * @return the end position of the given token
   */
  static int endOfToken(Token token) {
    return token.charPos + token.val.length();
  }

  /**
   * Returns the start position of the given token.
   *
   * @param token the token to get the start position of
   * @return the start position of the given token
   */
  static CodePosition tokenStartPosition(Token token) {
    return new CodePosition(token.line, startOfToken(token));
  }

  /**
   * Returns the end position of the given token.
   *
   * @param token the token to get the end position of
   * @return the end position of the given token
   */
  static CodePosition tokenEndPosition(Token token) {
    return new CodePosition(token.line, endOfToken(token));
  }

  /**
   * Returns the block begin position, depending on the block type and whether the block is a single statement.
   * <p>
   * It returns either the end position of the current token or the start position of the next token.
   *
   * @param parser            the parser object
   * @param blockType         the type of the block
   * @param isSingleStatement whether the block is a single statement
   * @return the block begin position
   */
  public static CodePosition getBlockBegPos(Parser parser, BlockType blockType, boolean isSingleStatement) {
    if (blockType == BlockType.COLON_CASE || isSingleStatement) {
      return tokenEndPosition(parser.t);
    } else { // la == '{'
      return tokenStartPosition(parser.la);
    }
  }

  /**
   * Returns the increment insert offset, depending on the block type and whether the block is a single statement.
   * <p>
   * It returns either 0 or the difference between the end position of the lookahead token and the block begin position.
   *
   * @param parser            the parser object
   * @param blockType         the type of the block
   * @param isSingleStatement whether the block is a single statement
   * @return the increment insert offset
   */
  public static int getIncInsertOffset(Parser parser, BlockType blockType, boolean isSingleStatement) {
    if (!isSingleStatement && blockType != BlockType.COLON_CASE) { // la == '{'
      return endOfToken(parser.la) - getBlockBegPos(parser, blockType, false).pos();
    } else {
      return 0;
    }
  }

  /**
   * Returns the region start token, depending on the block type and whether the block is a single statement.
   * <p>
   * It returns either the lookahead token or the one after.
   *
   * @param parser            the parser object
   * @param blockType         the type of the block
   * @param isSingleStatement whether the block is a single statement
   * @return the region start token
   */
  static Token getRegionStartToken(Parser parser, BlockType blockType, boolean isSingleStatement) {
    parser.scanner.ResetPeek();
    if (blockType == BlockType.COLON_CASE || isSingleStatement) {
      return parser.la;
    } else { // la == '{'
      return parser.scanner.Peek();
    }
  }

}
