package org.matwoess.jsourceprofiler.tool.instrument;

import org.junit.jupiter.api.Test;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;

import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.model.BlockType.BLOCK;
import static org.matwoess.jsourceprofiler.tool.model.ClassType.ANONYMOUS;
import static org.matwoess.jsourceprofiler.tool.model.ClassType.LOCAL;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.RETURN;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.THROW;

public class LocalClassesTest {

  @Test
  public void testEnumInMainMethod() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            enum StatusCode {
              OK, UNAUTHORIZED, FORBIDDEN, NOTFOUND
            }
            StatusCode statusCode = StatusCode.FORBIDDEN;
            if (statusCode != StatusCode.FORBIDDEN) {
              throw new RuntimeException("strange");
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jClass(LOCAL, "StatusCode"),
            jMethod("main", 2, 10, 61, 285,
                jBlock(BLOCK, 7, 9, 229, 281).withControlBreak(THROW)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testInterfaceInIfBlockAndAnonymousInstantiation() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            if ((2 + 6) % 2 == 0) {
              interface IGreeter {
                default void greet() {
                  System.out.println("Hello there.");
                }
                void greetPerson(Object person);
              }
              IGreeter greeter = new IGreeter() {
                @Override
                public void greetPerson(Object person) {
                  System.out.println("Hello " + person.toString() + ".");
                }
              };
              greeter.greetPerson(IGreeter.class);
            }
            if (true) return;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jClass(LOCAL, "IGreeter",
                jMethod("greet", 5, 7, 147, 204),
                jMethod("greetPerson")
            ),
            jClass(ANONYMOUS, null,
                jMethod("greetPerson", 12, 14, 361, 438)
            ),
            jMethod("main", 2, 19, 61, 522,
                jBlock(BLOCK, 3, 17, 89, 496),
                jSsBlock(BLOCK, 18, 18, 510, 518).withControlBreak(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testLocalClassInAnonymousClassMethod() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            final Comparator<String> lengthComparator = new Comparator<>() {
              @Override
              public int compare(String s1, String s2) {
                class CompHelper {
                  int comp(String s1, String s2) {
                    return s1.length() - s2.length();
                  }
                }
                CompHelper helper = new CompHelper();
                helper.comp(s1, s2);
                return helper.comp(s1, s2);
              }
            };
            lengthComparator.compare("word", "12345");
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jClass(ANONYMOUS, null,
                jClass(LOCAL, "CompHelper",
                    jMethod("comp", 7, 9, 265, 324).withControlBreak(RETURN)
                ),
                jMethod("compare", 5, 14, 195, 453).withControlBreak(RETURN)
            ),
            jMethod("main", 2, 17, 61, 511)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
