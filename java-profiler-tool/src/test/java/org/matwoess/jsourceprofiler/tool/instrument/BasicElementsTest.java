package org.matwoess.jsourceprofiler.tool.instrument;

import org.junit.jupiter.api.Test;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;

import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.model.BlockType.*;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.*;

public class BasicElementsTest {
  @Test
  public void testEmptyClass() {
    String fileContent = """
        class Empty {
        }""";
    JavaFile expected = jFile(
        jClass("Empty")
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testHelloWorld() {
    String fileContent = """
        public class HelloWorld {
          public static void main(String[] args) {
            System.out.println("Hello World!");
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("HelloWorld",
            jMethod("main", 2, 4, 67, 112)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testStaticBlock() {
    String fileContent = """
        public class Static {
          static int x;
          
          static {
            x = 0;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Static",
            jBlock(STATIC, 4, 6, 48, 64)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEmptyLoop() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            for (int i = 0; i < 10; i++) {}
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 4, 61, 102,
                jBlock(LOOP, 3, 3, 96, 98)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testForLoopWithIfs() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
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
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 15, 61, 287,
                jBlock(LOOP, 5, 13, 143, 251,
                    jBlock(BLOCK, 7, 9, 180, 202),
                    jBlock(BLOCK, 10, 12, 221, 245).withControlBreak(BREAK)
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testWhileAndDoWhileLoop() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 100;
            while (x > 0) {
              x -= 10;
              do {
                x += 3;
              } while ((x % 2) != 0);
            }
            System.out.println("x=" + x);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 11, 61, 215,
                jBlock(LOOP, 4, 9, 98, 177,
                    jBlock(LOOP, 6, 8, 124, 149)
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testTryCatchFinally() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
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
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 14, 61, 375,
                jBlock(TRY, 4, 6, 87, 111),
                jBlock(BLOCK, 6, 8, 143, 205),
                jBlock(BLOCK, 8, 10, 234, 307),
                jBlock(BLOCK, 10, 12, 316, 337)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTryWithResourceStatement() {
    String fileContent = """
        public class TryWithResource {
          static class MyClosable implements AutoCloseable {
            public void open() {
              System.out.println("Opening resource...");
            }
          
            public void close() throws Exception {
              System.out.println("closing resource...");
            }
          }
          
          public static void main(String[] args) {
            try (MyClosable resource = new MyClosable()) {
              resource.open();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass("TryWithResource",
            jClass("MyClosable",
                jMethod("open", 3, 5, 107, 163),
                jMethod("close", 7, 9, 206, 262)
            ),
            jMethod("main", 12, 18, 309, 459,
                jBlock(TRY, 13, 15, 360, 390),
                jBlock(BLOCK, 15, 17, 411, 455).withControlBreak(THROW)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testComments() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            // Testing comments
            /* begin comment /* no nested multi-line comments
            // but single line ones possible
            // Block comments start with "/*", end with "*" followed by "/".
            end comments */
            String s = getTempString(1);
          }
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
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 9, 61, 303),
            jMethod("getTempString", 17, 19, 649, 709).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testStatementBeginningWithStringLiteral() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            // ignoring result
            "Some string.".split(" ");
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 5, 61, 120)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testTernaryOperatorInReturn() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            if (false) {
              return 0;
            }
            return false ? -1 : 0;
          }
          public void doNothing() {}
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 7, 61, 132,
                jBlock(BLOCK, 3, 5, 78, 101).withControlBreak(RETURN)
            ).withControlBreak(RETURN),
            jMethod("doNothing", 8, 8, 159, 161)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTernaryOperator() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int number = 6;
            String msg = (number % 2 == 0)
                ? "Dividable by 2"
                : "Not dividable by 2";
            System.out.println(msg.contains("2") ? "2 appears" : "");
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 8, 61, 242)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testLabels() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 1;
            outer:
            while (true) {
              inner: while (true) {
                if (x == 1) {
                  x++;
                  break inner;
                } else {
                  break outer;
                }
              }
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 15, 61, 263,
                jBlock(LOOP, 5, 14, 106, 259,
                    jBlock(LOOP, 6, 13, 134, 253,
                        jBlock(BLOCK, 7, 10, 156, 205).withControlBreak(BREAK, "inner"),
                        jBlock(BLOCK, 10, 12, 211, 245).withControlBreak(BREAK, "outer")
                    )
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testIncrementAndDecrement() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 1;
            while (true) {
              if (x == 1) {
                --x;
                ++x;
                ++(x);
              } else {
                break;
              }
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 13, 61, 205,
                jBlock(LOOP, 4, 12, 95, 201,
                    jBlock(BLOCK, 5, 9, 115, 165),
                    jBlock(BLOCK, 9, 11, 171, 195).withControlBreak(BREAK)
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testGenericVariableInstantiation() {
    String fileContent = """
        class GenericInstantiation {
          private Map<String, Set<Integer>> map = new HashMap<String, Set<Integer>>();
          void fill() {
            map = new HashMap<String, Set<Integer>>()};
            map.put("Hello", Set.of(1, 2, 3));
            map.get("Hello");
          }
          public static void main(String[] args) {
            new GenericInstantiation().fill();
          }
        }""";
    JavaFile expected = jFile(
        jClass("GenericInstantiation",
            jMethod("fill", 3, 7, 122, 236),
            jMethod("main", 8, 10, 278, 322)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testAssertStatement() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 1;
            assert x == 1;
            int sum = "Hello".chars().map(ch -> ch + 2).sum();
            assert sum > 0 : "sum is: " + sum;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 7, 61, 194,
                jSsBlock(LAMBDA, 5, 5, 136, 143)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testThrowWithClassCastAndInstanceOf() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            try {
              int i = 5 / 0;
            } catch (Exception ex) {
              if (ex instanceof ArithmeticException) {
                throw (ArithmeticException) ex;
              } else if (ex instanceof ClassCastException classEx) {
                throw classEx;
              }
              throw new RuntimeException("Other exception: " + ex.getMessage());
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 13, 61, 384,
                jBlock(TRY, 3, 5, 71, 99),
                jBlock(BLOCK, 5, 12, 121, 380,
                    jBlock(BLOCK, 6, 8, 168, 217).withControlBreak(THROW),
                    jBlock(BLOCK, 8, 10, 269, 301).withControlBreak(THROW)
                ).withControlBreak(THROW)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testGenericArrayInstantiations() {
    String fileContent = """
        class GenArrays<T> {
          private static GenArrays<?>[] genArray = new GenArrays<?>[0];
          private static Class<?>[] classArray = new Class<?>[] {
              GenArrays.class, String.class
          };
            
          public static void main(String[] args) {
            genArray = new GenArrays<?>[6];
            genArray[0] = new GenArrays<String>();
            genArray[1] = new GenArrays<Map<Integer, Map<String, Void>>>();
            System.out.println(getFirstEntry());
            classArray[0] = GenArrays.class;
          }
            
          public static GenArrays<?> getFirstEntry() {
            return genArray[0];
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("GenArrays",
            jMethod("main", 7, 13, 226, 456),
            jMethod("getFirstEntry", 15, 17, 503, 532).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSynchronizedBlock() {
    String fileContent = """
        class SyncBlocks {
          synchronized int syncMethod() {
            Integer i = 1;
            synchronized (this) {
              System.out.println("in sync block");
              String s = ";";
              if (!s.isBlank()) {
                synchronized (s) {
                  System.out.println(s);
                }
              }
            }
            if (i > 0) {
              synchronized (i) {
                return i;
              }
            }
            return 0;
          }
          String getSyncValue() {
            int val = 0;
            int syncVal = syncMethod();
            if (syncVal != 0) {
              val = syncVal;
            }
            return String.valueOf(val);
          }
          public static void main(String[] args) {
            System.out.println(new SyncBlocks().getSyncValue());
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("SyncBlocks",
            jMethod("syncMethod", 2, 19, 51, 364,
                jBlock(BLOCK, 4, 12, 96, 272,
                    jBlock(BLOCK, 7, 11, 187, 266,
                        jBlock(BLOCK, 8, 10, 214, 258)
                    )
                ),
                jBlock(BLOCK, 13, 17, 288, 346,
                    jBlock(BLOCK, 14, 16, 313, 340).withControlBreak(RETURN)
                )
            ).withControlBreak(RETURN),
            jMethod("getSyncValue", 20, 27, 389, 526,
                jBlock(BLOCK, 23, 25, 462, 490)
            ).withControlBreak(RETURN),
            jMethod("main", 28, 30, 568, 630)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
