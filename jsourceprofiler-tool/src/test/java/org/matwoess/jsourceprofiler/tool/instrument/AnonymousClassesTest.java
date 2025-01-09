package org.matwoess.jsourceprofiler.tool.instrument;

import org.matwoess.jsourceprofiler.tool.model.JClass;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.model.BlockType.BLOCK;
import static org.matwoess.jsourceprofiler.tool.model.ClassType.ANONYMOUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.RETURN;

public class AnonymousClassesTest {
  @Test
  public void testAsArgumentInClassLevelMethod() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
          }
          static File firstJavaFile(Path directory) {
            File[] allJavaFiles = directory.toFile().listFiles(new FilenameFilter() {
              boolean isTrue = false;
               
              @Override
              public boolean accept(File file, String name) {
                isTrue = returnTrue();
                return name.endsWith(".java");
              }
               
              class X {
                static void methodInX() {
                  System.out.println("Hello from inside a nested class in an anonymous class.");
                }
              }
               
              public boolean returnTrue() {
                X.methodInX();
                return true;
              }
            });
            if (allJavaFiles != null && allJavaFiles.length > 0) {
              return allJavaFiles[0];
            } else {
              return null;
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 3, 61, 66),
            jMethod("firstJavaFile", 4, 30, 111, 755,
                jBlock(BLOCK, 25, 27, 682, 719).withControlBreak(RETURN),
                jBlock(BLOCK, 27, 29, 725, 751).withControlBreak(RETURN)
            ),
            jClass(ANONYMOUS, null,
                jClass("X",
                    jMethod("methodInX", 15, 17, 419, 519)
                ),
                jMethod("accept", 9, 12, 290, 369).withControlBreak(RETURN),
                jMethod("returnTrue", 20, 23, 563, 616).withControlBreak(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    JClass innerClass = expected.topLevelClasses.get(0).innerClasses.get(0);
    assertEquals("Main$1", innerClass.getName());
    assertEquals("Main$1$X", innerClass.innerClasses.get(0).getName());
  }

  @Test
  public void testAs2ndArgumentInClassLevelMethodWithGenericType() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
          }
          static List<Integer> getSortedIntegers(List<Integer> arrayList) {
              Collections.sort(arrayList, new Comparator<Integer>() {
                @Override
                public int compare(Integer i1, Integer i2) {
                  if (i1.equals(i2)) {
                    return 0;
                  }
                 
                  return i1 < i2 ? -1 : 1;
                }
              });
              return arrayList;
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 3, 61, 66),
            jMethod("getSortedIntegers", 4, 16, 133, 418).withControlBreak(RETURN),
            jClass(ANONYMOUS, null,
                jMethod("compare", 7, 13, 266, 378,
                    jBlock(BLOCK, 8, 10, 297, 332).withControlBreak(RETURN)
                ).withControlBreak(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    assertEquals("Main$1", expected.topLevelClasses.get(0).innerClasses.get(0).getName());
  }

  @Test
  public void testAsStatementStartInMethod() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            new Main() {
              @Override
              public int hashCode() {
                return super.hashCode();
              }
            };
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 9, 61, 177),
            jClass(ANONYMOUS, null,
                jMethod("hashCode", 5, 7, 124, 166).withControlBreak(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    assertEquals("Main$1", expected.topLevelClasses.get(0).innerClasses.get(0).getName());
  }

}
