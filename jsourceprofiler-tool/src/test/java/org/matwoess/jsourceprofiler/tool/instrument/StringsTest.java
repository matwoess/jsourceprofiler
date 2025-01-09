package org.matwoess.jsourceprofiler.tool.instrument;

import org.junit.jupiter.api.Test;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;

import java.nio.charset.StandardCharsets;

import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.model.BlockType.BLOCK;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.RETURN;

public class StringsTest {
  @Test
  public void testEscapedCharLiterals() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            // ignoring result
            char c = '\\"'; c = '\\'';
            c = '\\n'; c = '\\r'; c = '\\t';
            c = '\\\\';
            c = '\\b'; c = '\\s'; c = '\\f';
            c = '\\0'; c = '\\1'; c = '\\2'; c = '\\3';
            c = '\\6'; c = '\\67';
            c = '\\uFF1A'; c = '\\uuu231A';
            c = '\\064'; c = '\\377';
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 12, 61, 331)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testStringLiteralWithEscapedCharacters() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            String s = "''''\\"\\"\\"\\r\\n\\t\\"\\f\\b\\s_asdf";
            s = "\\u42FA_\\uuuADA1_\\1_\\155adsf\\6_\\43_Text";
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 5, 61, 164)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMethodWithDoubleBackslashInString() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            boolean b = "Text\\\\".endsWith("\\\\");
            if (b) {
              System.out.println("does end with \\\\");
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 7, 61, 172,
                jBlock(BLOCK, 4, 6, 115, 168)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTextBlocks() {
    String fileContent = """
        class TextBlocks {
          static String tBlock = ""\"
              Line1,
              Line2
              ""\";
          public static void main(String[] args) {
            tBlock += ""\"
                ,
                Line3,
                Line4
                ""\";
          }
          public static String getTextBlock() {
            return tBlock;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("TextBlocks",
            jMethod("main", 6, 12, 125, 200),
            jMethod("getTextBlock", 13, 15, 239, 263).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTextBlocksContainingStringsAndEscapeSequences() {
    String fileContent = """
        class TextBlocksAndSubStrings {
          static String tBlock = ""\"
              "Line1 containing character 'a'",
              code = "String s = "some text"";
              ""\";
          public static void main(String[] args) {
            tBlock += ""\"
                "More text \t\034"
                ""\";
                if (tBlock.length() > 65) {
                  return;
                }
          }
          public static String getTextBlock() {
            return tBlock;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("TextBlocksAndSubStrings",
            jMethod("main", 6, 13, 192, 315,
                jBlock(BLOCK, 10, 12, 282, 311).withControlBreak(RETURN)
            ),
            jMethod("getTextBlock", 14, 16, 354, 378).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testTextBlocksContainingTextBlocks() {
    String fileContent = """
        class TextBlocksAndSubTextBlocks {
          static String tBlock = ""\"
              ""\\"
              "TextBlock",
              ""\\"
              ""\";
          public static void main(String[] args) {
            tBlock += ""\"
                ""\\"
                ""\\\\"
                "TextBlock",
                ""\\\\"
                ""\\"
                ""\";
          }
          public static String getTextBlock() {
            return tBlock;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("TextBlocksAndSubTextBlocks",
            jMethod("main", 7, 15, 157, 268),
            jMethod("getTextBlock", 16, 18, 307, 331).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testTextBlocksContainingXMLStructure() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            System.out.println(""\"
                   <body>
                   <span>  static String startEndWithBSlash = "\\\\\\\\asdf\\\\tqwerty\\\\\\\\";</span>
                   <span>  static String emptyTextBlock = ""\\"</span>
                   <span>  ""\\";</span>
                   <span>  static String withNestedBlock = ""\\"</span>
                   <span>      Text with</span>
                   <span>      multiple (!)</span>
                   <span>      lines!\\\\n</span>
                   <span>      can contain "strings", \\\\"escaped strings\\\\"</span>
                   <span>      and \\\\t""\\\\"</span>
                   <span>      Text blocks!</span>
                   <span>      with ""\\\\\\\\\\\\\\\\"</span>
                   <span>        "nested(!) text blocks",</span>
                   <span>        ""\\\\\\\\\\\\\\\\"</span>
                   <span>      \\\\""\\"</span>
                   <span>      ""\\";</span>
                   <span></span>
                   </body>
               ""\");
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 23, 61, 916)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTextBlocksWithDifferentEndings() {
    String fileContent = new String("""
        class TextBlocksWithDifferentEndings {
          public static void main(String[] args) {
            System.out.println(printStrings());
          }
          
          static String printStrings() {
            {
              System.out.print(""\"
                  ""\");
            } {
              System.out.println(""\"
                  x""\");
            } {
              System.out.println(""\"
                  \\""\"");
            } {
              System.out.println(""\"
                  "str1" "str2"
                  ""\");
            } {
              System.out.println(""\"
                  ""str1"" \\"\\"str2\\"\\"
                  ""\");
            } {
              System.out.println(""\"
                  ""str"" ßß$"!)(@æſĸðf\\t\\n\\
                  "\\""\"");
            }
            return ""\"
                \\""\"
                nested "str"
                ""\\"
                ""\";
          }
        }
        """.getBytes(), StandardCharsets.UTF_8);
    JavaFile expected = jFile(
        jClass("TextBlocksWithDifferentEndings",
            jMethod("main", 2, 4, 80, 125),
            jMethod("printStrings", 6, 34, 158, 663,
                jBlock(BLOCK, 7, 10, 164, 214),
                jBlock(BLOCK, 10, 13, 215, 268),
                jBlock(BLOCK, 13, 16, 269, 323),
                jBlock(BLOCK, 16, 20, 324, 400),
                jBlock(BLOCK, 20, 24, 401, 485),
                jBlock(BLOCK, 24, 28, 486, 584)
            ).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTextBlocksWithQuotesBeforeInlineEnd() {
    String fileContent = """
        class TextBlocksWithQuotesBeforeInlineEnd {
          static String tBlock = ""\"
              \\"\\"\\"
              "TextBlock",
              \\""\"\\"
              ""\\""\"";
              
          public static void main(String[] args) {
            tBlock += ""\"
                \\""\\""\"";
            if (args.length == 0) {
              tBlock += ""\"
                  \\""\"
                  TextBlock",
                  ""
                  \\""\\"
                  "\\""
                  \\"\\"\\"
                  ""\\""\"";
            }
          }
          
          public static String getTextBlock() {
            return tBlock;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("TextBlocksWithQuotesBeforeInlineEnd",
            jMethod("main", 8, 21, 175, 387,
                jBlock(BLOCK, 11, 20, 239, 383)
            ),
            jMethod("getTextBlock", 23, 25, 427, 451).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

}
