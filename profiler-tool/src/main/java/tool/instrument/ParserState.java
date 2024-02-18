package tool.instrument;

import tool.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static tool.instrument.Util.*;

/**
 * Companion class for the {@link Parser}, generated by the attributed grammar in <code>JavaFile.atg</code>.
 * It is the most important class of the instrumentation tool.
 * <p>
 * It is used to keep track of the current parser state
 * and contains methods to enter and leave parsed program components.
 * Each enter and leave method is responsible for building the metadata model of the parsed Java file.
 * <p>
 * Additionally, it contains methods to register labels and control flow breaks,
 * setting the package name and all the logic handling the starting end ending of code regions.
 */
public class ParserState {
  /**
   * The parser object this state belongs to.
   */
  private final Parser parser;
  /**
   * The logger used to print verbose parsing log messages. Can be activated by setting {@link Logger#active} to true.
   */
  final Logger logger;

  /**
   * The character position for the beginning of the imports (after the package declaration).
   */
  int beginOfImports = 0;
  /**
   * The package name of the parsed Java file.
   */
  String packageName = null;

  /**
   * The list containing all top level classes of the parsed Java file.
   */
  final List<JClass> topLevelClasses = new ArrayList<>();
  /**
   * The list containing all blocks in order of appearance.
   */
  final List<Block> allBlocks = new ArrayList<>();

  // private globals for the parsing process
  private final Stack<Block> blockBackupStack = new Stack<>();
  private JClass curClass = null;
  private Method curMeth = null;
  private Block curBlock = null;
  private int curBlockId = 0;
  private CodeRegion curCodeRegion;

  private final List<String> curLabels = new ArrayList<>();

  /**
   * Creates a new ParserState, linked to the given parser.
   * Additionally, it creates a new logger object.
   *
   * @param p the parser object to link to
   */
  public ParserState(Parser p) {
    parser = p;
    logger = new Logger(p);
  }

  /**
   * Sets the package name to the given list of package name parts.
   * The parts will be joined with a dot.
   * <p>
   * Additionally, it sets the begin of imports to the end of the current token.
   *
   * @param packageName the list of string pieces for the fully qualified package name
   */
  void setPackageName(List<String> packageName) {
    this.packageName = String.join(".", packageName);
    this.beginOfImports = endOfToken(parser.t);
  }

  /**
   * Sets the incInsertOffset of the current block to the end of the current token.
   * <p>
   * Called for constructor blocks,
   * because the <code>super()</code> call must always be the first statement in the block.
   * Also used with <code>this</code> calls for overloaded constructor methods.
   */
  void markEndOfSuperCall() {
    assert curClass != null && curMeth != null && curBlock.blockType == BlockType.CONSTRUCTOR;
    curBlock.incInsertOffset = endOfToken(parser.t) - curBlock.beg.pos();
  }

  /**
   * Registers the lookahead token value as a label for the next code block definition.
   */
  void registerLabel() {
    curLabels.add(parser.la.val);
  }

  /**
   * Registers the imminent control break in the current block and propagates it to the outer blocks
   * of the current scope as an inner control break using {@link #registerControlBreakInOuterBlocks}.
   */
  void registerControlBreak() {
    ControlBreak controlBreak;
    if ((parser.t.val.equals("break") || parser.t.val.equals("continue")) && parser.la.kind == Parser._ident) {
      controlBreak = ControlBreak.fromTokenWithLabel(parser.t.val, parser.la.val);
    } else {
      controlBreak = ControlBreak.fromToken(parser.t.val);
    }
    curBlock.controlBreak = controlBreak;
    logger.log("> found control break: %s", controlBreak);
    registerControlBreakInOuterBlocks(controlBreak);
  }

  /**
   * Registers the given control flow break in the parent blocks as inner control break block.
   * <p>
   * If the current block "catches" the control break, it will not be propagated further.
   * <p>
   * To determine when the propagation should stop {@link ControlBreak#stopPropagationAt)} is used.
   *
   * @param controlBreak the control break to register
   */
  private void registerControlBreakInOuterBlocks(ControlBreak controlBreak) {
    if (controlBreak.stopPropagationAt(curBlock)) {
      return; // do not propagate, if `curBlock` catches the control break immediately (e.g. switch case with break)
    }
    for (Block block = curBlock.parentBlock; block != null; block = block.parentBlock) {
      block.registerInnerControlBreak(curBlock);
      if (controlBreak.stopPropagationAt(block)) {
        break;
      }
    }
  }


  /**
   * Enters a class with the given class type and name.
   * <p>
   * Creates a new {@link JClass} object and sets it as the current class.
   * If a code region is currently active, it will be ended (important for anonymous and local classes).
   *
   * @param classType the of the class (one of {@link ClassType})
   * @param className the name of the class
   */
  void enterClass(ClassType classType, String className) {
    endCodeRegion();
    JClass newClass = new JClass(className, classType);
    newClass.packageName = packageName;
    newClass.setParentClass(curClass);
    if (curClass == null) {
      topLevelClasses.add(newClass);
    }
    if (curBlock != null) {
      blockBackupStack.push(curBlock);
      curBlock = null;
      curMeth = null;
    }
    logger.enter(newClass);
    curClass = newClass;
  }

  /**
   * Leaves the current class and restores its parent class as the current class.
   * <p>
   * If the current class is an anonymous or local class, the current block is restored to the block before the
   * anonymous or local class.
   * The {@link #reenterBlock} method is called to reenter the block and start a new code region.
   */
  void leaveClass() {
    logger.leave(curClass);
    if (curClass.classType == ClassType.ANONYMOUS || curClass.classType == ClassType.LOCAL) {
      if (!blockBackupStack.isEmpty()) {
        curBlock = blockBackupStack.pop();
        curMeth = curBlock.method;
        reenterBlock(curBlock.blockType, true); // always true, because leaveClass is called after '}'
      }
    }
    curClass = curClass.parentClass;
  }

  /**
   * Creates a new method object with the given name and sets it as the current method.
   * <p>
   * The method is also added to the current class's list of methods using {@link Method#setParentClass}.
   * @param methName the name of the method that is entered
   */
  void enterMethod(String methName) {
    assert curClass != null;
    curMeth = new Method(methName);
    curMeth.setParentClass(curClass);
    logger.enter(curMeth);
  }

  /**
   * Registers an abstract method with the given name in the current class.
   * @param methName the name of the abstract method
   */
  void registerAbstractMethod(String methName) {
    assert curClass != null;
    new Method(methName).setParentClass(curClass);
    logger.log("> found abstract method: %s", methName);
  }

  /**
   * Leaves the current method.
   */
  void leaveMethod() {
    assert curMeth != null;
    logger.leave(curMeth);
    curMeth = null;
  }

  /**
   * Enters a block with a given block type.
   * <p>
   * Used for common blocks with curly braces.
   * Single-statement blocks have their own method {@link #enterSSBlock}.
   * <p>
   * Simply calls the {@link #enterBlock} method with the given block type and <code>false</code> for missing braces.
   * <p>
   * Contains special handling for detecting constructor blocks.
   *
   * @param blockType the type of the block (one of {@link BlockType})
   */
  void enterBlock(BlockType blockType) {
    if (blockType == BlockType.METHOD) {
      if (curMeth.name.equals(curClass.name)) { // TODO: not entirely correct, also must not have return type
        blockType = BlockType.CONSTRUCTOR;
      }
    }
    enterBlock(blockType, false);
  }

  /**
   * Enters a single statement block with a given block type.
   * <p>
   * Simply calls the {@link #enterBlock} method with the given block type and <code>true</code> for missing braces.
   *
   * @param blockType the type of the block (one of {@link BlockType})
   */
  void enterSSBlock(BlockType blockType) {
    enterBlock(blockType, true);
  }

  /**
   * Called when colon-case switch blocks are entered. They never start with curly braces.
   * <p>
   * Simply calls the {@link #enterBlock} method with the <code>COLON_CASE</code> block type
   * and <code>true</code> for missing braces.
   */
  void enterSwitchColonCase() {
    assert curBlock != null && curBlock.blockType.isSwitch();
    enterBlock(BlockType.COLON_CASE, true);
  }

  /**
   * Enters a block with a given block type and a parameter for whether it is a single statement block.
   * <p>
   * First we end the current code region (if any) and create a new block object.
   * The block is then added to the list of all blocks and the {@link #curBlock} is set to this new block.
   * Afterward, the {@link #startCodeRegion} method is called to start a new code region.
   *
   * @param blockType     the type of the block (one of {@link BlockType})
   * @param missingBraces whether the block is a single statement block
   */
  private void enterBlock(BlockType blockType, boolean missingBraces) {
    assert curClass != null;
    endCodeRegion();
    Block newBlock = new Block(blockType);
    newBlock.id = curBlockId++;
    newBlock.setParentBlock(curBlock);
    newBlock.setParentMethod(curMeth);
    newBlock.setParentClass(curClass);
    newBlock.isSingleStatement = blockType != BlockType.COLON_CASE && missingBraces;
    newBlock.beg = Util.getBlockBegPos(parser, blockType, missingBraces);
    newBlock.incInsertOffset = Util.getIncInsertOffset(parser, blockType, missingBraces);
    allBlocks.add(newBlock);
    if (!curLabels.isEmpty()) {
      newBlock.labels.addAll(curLabels);
      curLabels.clear();
    }
    logger.enter(newBlock);
    curBlock = newBlock;
    startCodeRegion(blockType, missingBraces);
  }

  /**
   * Leaves the current block of a given type.
   *
   * @param blockType the type of the block (one of {@link BlockType})
   */
  void leaveBlock(BlockType blockType) {
    leaveBlock(blockType, curBlock.hasNoBraces());
  }

  /**
   * Leaves the current block of a given type and a parameter for whether it was a single statement block.
   * <p>
   * The current code region is ended and the {@link #curBlock} is set to the parent block.
   * The {@link #reenterBlock} method is called to reenter the parent block and start a new code region.
   *
   * @param blockType     the type of the block (one of {@link BlockType})
   * @param missingBraces whether the block was a single statement block
   */
  private void leaveBlock(BlockType blockType, boolean missingBraces) {
    curBlock.end = tokenEndPosition(missingBraces ? parser.t : parser.la);
    logger.leave(curBlock);
    endCodeRegion();
    curBlock = curBlock.parentBlock;
    reenterBlock(blockType, missingBraces);
  }

  /**
   * Start a new code region for a given block type and a parameter for whether braces are missing.
   * <p>
   * The region is only started if the next token is a valid code region start token.
   *
   * @param blockType     the type of the block (one of {@link BlockType})
   * @param missingBraces whether the block has missing braces. Used to determine the start token of the code region.
   */
  private void startCodeRegion(BlockType blockType, boolean missingBraces) {
    assert curCodeRegion == null;
    Token nextToken = getRegionStartToken(parser, blockType, missingBraces);
    if (validCodeRegionStartToken(nextToken)) {
      curCodeRegion = new CodeRegion();
      curCodeRegion.beg = tokenStartPosition(nextToken);
      curCodeRegion.block = curBlock;
      logger.enter(curCodeRegion);
    }
  }

  /**
   * Returns whether the given token is a valid code region start token.
   * <p>
   * Currently, the only invalid tokens are <code>else</code>, <code>catch</code> and <code>finally</code>.
   *
   * @param nextToken the token to check
   * @return whether the given token is a valid code region start token
   */
  private boolean validCodeRegionStartToken(Token nextToken) {
    if (curBlock.blockType.hasNoCounter()) return false;
    return !nextToken.val.equals("else") && !nextToken.val.equals("catch") && !nextToken.val.equals("finally");
  }

  /**
   * Ends the current code region with the end position of the current token and stores the region in the current block.
   */
  private void endCodeRegion() {
    if (curCodeRegion == null) return;
    curCodeRegion.end = tokenEndPosition(parser.t);
    logger.leave(curCodeRegion);
    // assert !curCodeRegion.end.equals(curCodeRegion.beg); // TODO: remove, true for empty block "{}"
    curBlock.addCodeRegion(curCodeRegion);
    curCodeRegion = null;
  }

  /**
   * Reenters the parent block after leaving an inner one.
   * <p>
   * If the parent block was null, we do not reenter.
   * <p>
   * Also, if the next token is a valid code region start token, we start a new code region.
   *
   * @param blockType     the type of the block (one of {@link BlockType})
   * @param missingBraces whether the block has missing braces. Used to determine the start token of the code region.
   */
  private void reenterBlock(BlockType blockType, boolean missingBraces) {
    if (curBlock == null) return;
    Token nextToken = missingBraces ? parser.la : parser.scanner.Peek();
    if (validCodeRegionStartToken(nextToken)) {
      startCodeRegion(blockType, missingBraces);
      curCodeRegion.dependentBlocks.addAll(curBlock.innerControlBreaks);
    }
  }

  /**
   * LL1 resolver method to determine whether the next two tokens are an ident followed by an opening parenthesis.
   *
   * @return whether the next two tokens are an ident followed by an opening parenthesis
   */
  boolean identAndLPar() {
    parser.scanner.ResetPeek();
    return parser.la.kind == Parser._ident && parser.scanner.Peek().val.equals("(");
  }

  /**
   * LL1 resolver method to determine whether the next two tokens are the current class name followed by an opening brace.
   *
   * @return whether the next two tokens are a class name followed by an opening curly brace
   */
  boolean classNameAndLBrace() {
    parser.scanner.ResetPeek();
    return parser.la.val.equals(curClass.name) && parser.scanner.Peek().val.equals("{");
  }

  /**
   * LL1 resolver method to determine whether the next two tokens are <code>static</code> followed by an opening brace.
   *
   * @return whether the next two tokens are <code>static</code> followed by an opening curly brace
   */
  boolean staticAndLBrace() {
    parser.scanner.ResetPeek();
    return parser.la.val.equals("static") && parser.scanner.Peek().val.equals("{");
  }

  /**
   * LL1 resolver method to determine whether the next two tokens are an ident followed by a colon.
   *
   * @return whether the next two tokens are ident and colon
   */
  boolean isLabel() {
    return parser.la.kind == Parser._ident && parser.scanner.Peek().val.equals(":");
  }

  /**
   * LL1 resolver method
   * to determine whether the next two tokens are <code>this</code> followed by an opening parenthesis.
   * This would suggest that an overloaded constructor is called inside a constructor block.
   *
   * @return whether the next two tokens are <code>this</code> and an opening parenthesis
   */
  boolean thisAndLPar() {
    return parser.la.val.equals("this") && parser.scanner.Peek().val.equals("(");
  }

  /**
   * Helper method to determine whether the following code is an expression
   * (i.e., the current token is an assignment operator, <code>return</code> or <code>yield</code>).
   *
   * @return whether the following code is an assigning expression
   */
  boolean isAssignment() {
    return parser.t.val.equals("=") || parser.t.val.equals("return") || parser.t.val.equals("yield");
  }

  /**
   * LL1 resolver method to determine whether the next two tokens are <code>class</code>, <code>interface</code> or
   * <code>record</code> followed by an ident.
   * This would suggest that a class definition is following.
   * <p>
   * Additionally, it checks whether the current token is not a dot.
   * This is necessary to avoid false positives when
   * <code>ident.class</code> is used to access the class object of a class.
   *
   * @return whether a class definition is following
   */
  boolean classDefWithNoLeadingDot() {
    parser.scanner.ResetPeek();
    return !parser.t.val.equals(".")
        && (parser.la.val.equals("class")
        || parser.la.val.equals("interface")
        || parser.la.val.equals("record") && parser.scanner.Peek().kind == Parser._ident);
  }
}
