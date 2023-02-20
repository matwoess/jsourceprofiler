package instrument;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestBasic {

  public String baseTemplate = """
      public class Main {
        public static void main(String[] args) {
          %s
        }
         %s
      }
      """;

  @Test
  public void TestEmptyProgram() {
    String emptyClass = """
        class Empty {
        }""";
    List<Parser.Block> blocks = getFoundBlocks(emptyClass);
    assertEquals(0, blocks.size());
  }

  @Test
  public void TestBeginOfImports() {
    String withoutPackage = """
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
        }""";
    int beginOfImports = getBeginOfImports(withoutPackage);
    assertEquals(0, beginOfImports);
    String withPackage = """
        package name.of._the_.package ;
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
        }""";
    beginOfImports = getBeginOfImports(withPackage);
    int lengthOfPackageDeclaration = "package name.of._the_.package ;".length();
    assertEquals(lengthOfPackageDeclaration, beginOfImports);
  }

  @Test
  public void TestHelloWorld() {
    String helloWorld = """
        public class HelloWorld {
          public static void main(String[] args) {
            System.out.println("Hello World!");
          }
        }
        """;
    List<Parser.Block> blocks = getFoundBlocks(helloWorld);
    assertEquals(1, blocks.size());
    Parser.Class clazz = new Parser.Class("HelloWorld", true);
    Parser.Method meth = new Parser.Method("main", true);
    Parser.Block expectedBlock = getMethodBlock(clazz, meth, 2, 4, 68, 112);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestStaticBlock() {
    String staticBlock = """
        public class Static {
          static int x;
          
          static {
            x = 0;
          }
        }
        """;
    List<Parser.Block> blocks = getFoundBlocks(staticBlock);
    assertEquals(1, blocks.size());
    Parser.Class clazz = new Parser.Class("Static");
    Parser.Method meth = new Parser.Method("static");
    Parser.Block expectedBlock = getBlock(clazz, meth, 4, 6, 49, 64);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestForLoopWithIfs() {
    String fileContent = String.format(baseTemplate, """
        String output = "finished";
        int x = 0;
        for (int i = 0; i < 10; i++) {
          x = 0;
          if (i % 2 == 0) {
            x++;
          }
          if (x > 10) {
            break;
          }
        }
        System.out.println(output);
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(4, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 16, 62, 244));
    expectedBlocks.add(getBlock(clazz, meth, 5, 13, 136, 211));
    expectedBlocks.add(getBlock(clazz, meth, 7, 9, 165, 178));
    expectedBlocks.add(getBlock(clazz, meth, 10, 12, 194, 209));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestWhileAndDoWhileLoop() {
    String fileContent = String.format(baseTemplate, """
        int x = 100;
        while (x > 0) {
          x -= 10;
          do {
            x += 3;
          } while ((x % 2) != 0);
        }
        System.out.println("x=" + x);
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 12, 62, 188));
    expectedBlocks.add(getBlock(clazz, meth, 4, 9, 95, 153));
    expectedBlocks.add(getBlock(clazz, meth, 6, 8, 113, 129));
    assertIterableEquals(expectedBlocks, blocks);
  }


  @Test
  public void TestTryCatchFinally() {
    String fileContent = String.format(baseTemplate, """
        int x = 50;
        try {
          x = x / 0;
        } catch (ArithmeticException ex) {
          System.out.println("Error: " + ex.getMessage());
        } catch (RuntimeException ex) {
          System.out.println("Unexpected error: " + ex.getMessage());
        } finally {
          x /= 2;
        }
        System.out.println("x=" + x);
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(5, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 15, 62, 336));
    expectedBlocks.add(getBlock(clazz, meth, 4, 6, 84, 99));
    expectedBlocks.add(getBlock(clazz, meth, 6, 8, 132, 185));
    expectedBlocks.add(getBlock(clazz, meth, 8, 10, 215, 279));
    expectedBlocks.add(getBlock(clazz, meth, 10, 12, 289, 301));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestSwitch() {
    String fileContent = String.format(baseTemplate, """
       int x = 1;
       switch (x) {
         case 1: {
           i += 3;
           break;
         }
         case 2: {}
         case 3: {
          i *= 2;
          i = i - 1;
         }
         case 4: {
           break;
         }
         default: { break; }
       }
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(6, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 20, 62, 239));
    expectedBlocks.add(getBlock(clazz, meth, 5, 8, 102, 129));
    expectedBlocks.add(getBlock(clazz, meth, 9, 9, 141, 142));
    expectedBlocks.add(getBlock(clazz, meth, 10, 13, 154, 183));
    expectedBlocks.add(getBlock(clazz, meth, 14, 16, 195, 210));
    expectedBlocks.add(getBlock(clazz, meth, 17, 17, 223, 232));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestComments() {
    String fileContent = String.format(baseTemplate, """
        // Testing comments
        /* begin comment /* no nested multi-line comments
        // but single line ones possible
        // Block comments start with "/*", end with "*" followed by "/".
        end comments */
        String s = getTempString(1);
         """, """
        /**
        This is a docstring:<br/>
        Method returns a string containing a given number.<br>
        Is called by the {@link #main(String[]) main} method in class {@link Main Main}.
        @param number the number which should be contained in the returned string.
        @returns a new string containing the number.
        */
       static String getTempString(int number) {
         return String.format("The number was %d", number);
       }
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 10, 62, 284));
    meth = new Parser.Method("getTempString", false);
    expectedBlocks.add(getMethodBlock(clazz, meth, 18, 20, 625, 680));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestStatementBeginningWithStringLiteral() {
    String fileContent = String.format(baseTemplate, """
        // ignoring result
        "Some string.".split(" ");
         """, "");
    "Some string.".split(" ");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 6, 62, 117));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
