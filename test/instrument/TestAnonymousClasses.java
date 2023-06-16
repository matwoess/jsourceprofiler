package instrument;

import model.Class;
import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.ProgramBuilder.*;
import static instrument.Util.baseTemplate;
import static instrument.Util.parseJavaFile;
import static model.BlockType.BLOCK;
import static model.BlockType.METHOD;
import static model.ClassType.ANONYMOUS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAnonymousClasses {
  @Test
  public void TestAsArgumentInClassLevelMethod() {
    String fileContent = String.format(baseTemplate, "", """
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
         """);
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 4, 62, 71)
            ),
            jMethod("firstJavaFile",
                jBlock(METHOD, 5, 31, 118, 715),
                jBlock(BLOCK, 26, 28, 653, 685),
                jBlock(BLOCK, 28, 30, 692, 713)
            ),
            jClass(ANONYMOUS, null, false,
                jMethod("accept",
                    jBlock(METHOD, 10, 13, 289, 361)
                ),
                jClass("X",
                    jMethod("methodInX",
                        jBlock(METHOD, 16, 18, 408, 503)
                    )
                ),
                jMethod("returnTrue",
                    jBlock(METHOD, 21, 24, 544, 590)
                )
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
    Class innerClass = expected.topLevelClasses.get(0).innerClasses.get(0);
    assertEquals("Main$1", innerClass.getName());
    assertEquals("Main$1$X", innerClass.innerClasses.get(0).getName());
  }

  @Test
  public void TestAs2ndArgumentInClassLevelMethodWithGenericType() {
    String fileContent = String.format(baseTemplate, "", """
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
        """);
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 4, 62, 71)
            ),
            jMethod("getSortedIntegers",
                jBlock(METHOD, 5, 17, 140, 380)
            ),
            jClass(ANONYMOUS, null, false,
                jMethod("compare",
                    jBlock(METHOD, 8, 14, 261, 352),
                    jBlock(BLOCK, 9, 11, 288, 314)
                )
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
    assertEquals("Main$1", expected.topLevelClasses.get(0).innerClasses.get(0).getName());
  }

  @Test
  public void TestAsStatementStartInMethod() {
    String fileContent = String.format(baseTemplate, """
        new Main() {
          @Override
          public int hashCode() {
            return super.hashCode();
          }
        };
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 10, 62, 158)
            ),
            jClass(ANONYMOUS, null, false,
                jMethod("hashCode",
                    jBlock(METHOD, 5, 7, 117, 150)
                )
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
    assertEquals("Main$1", expected.topLevelClasses.get(0).innerClasses.get(0).getName());
  }

}
