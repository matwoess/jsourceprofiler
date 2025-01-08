package org.matwoess.jsourceprofiler.tool.instrument;

import org.matwoess.jsourceprofiler.tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.model.ClassType.CLASS;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.RETURN;

public class InterfacesTest {
  @Test
  public void testAbstractMethodsNoBlock() {
    String fileContent = """
        interface AbstractMethods {
          String getName();
          public int getAge(int userId);
          abstract void printInfo(String userName, int age);
        }""";
    JavaFile expected = jFile(
        jClass("AbstractMethods")
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMembersWithInitBlocks() {
    String fileContent = """
        interface InitBlocks {
          int[] ints = new int[]{0, 1, 3};
          List<Float> floats = new ArrayList<>(Arrays.asList(0.5f, 3.4f));
          String[] strings = new String[]{
              String.format("%d", ints[1]),
              floats.get(1).toString(),
              "ASDF",
          };
          default void doNothing() {}
        }""";
    JavaFile expected = jFile(
        jClass("InitBlocks",
            jMethod("doNothing", 9, 9, 274, 276)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testDefaultAndStaticMethodsWithBlock() {
    String fileContent = """
        interface DefaultStatic {
          int x = 0;
          default String getName() {
            return "empty";
          }
          default public int getAge(int userId) {
            return userId / 2;
          }
          static void printInfo(String userName, int age) {
            System.out.printf("%s: %d\\n", userName, age);
          }
        }""";
    JavaFile expected = jFile(
        jClass("DefaultStatic",
            jMethod("getName", 3, 5, 66, 91).withControlBreak(RETURN),
            jMethod("getAge", 6, 8, 132, 160).withControlBreak(RETURN),
            jMethod("printInfo", 9, 11, 211, 266)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSubInterfacesAndClasses() {
    String fileContent = """
        public interface Interfaces extends Serializable {
          int x = 0;
          int get();
          public interface SubInterface extends Interfaces {
            @Override
            default public int get() {
              return SubClass.getXPlus1() + ints[0];
            }
            
            class SubClass {
              static int getXPlus1() {
                return x + 1;
              }
            }
          }
          
          class X implements SubInterface {
            void callGet() {
              get();
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass("Interfaces",
            jClass("SubInterface",
                jMethod("get", 6, 8, 173, 225).withControlBreak(RETURN),
                jClass(CLASS, "SubClass",
                    jMethod("getXPlus1", 11, 13, 277, 308).withControlBreak(RETURN)
                )
            ),
            jClass("X",
                jMethod("callGet", 18, 20, 375, 395)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMainEntryPointPublic() {
    String fileContent = """
        public interface WithMain {
          int get();
          
          class X implements WithMain {
            @Override
            public int get() {
              return 0;
            }
          }
          
          public static void main(String[] args) {
            WithMain x = new X();
            int result = x.get();
            System.out.println(result);
          }
        }""";
    JavaFile expected = jFile(
        jClass("WithMain",
            jClass("X",
                jMethod("get", 6, 8, 109, 132).withControlBreak(RETURN)
            ),
            jMethod("main", 11, 15, 179, 268)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMainEntryPointImplicitPublic() {
    String fileContent = """
        public interface InferredPublic {
          static void main(String[] args) {
            WithMain x = new X();
            int result = x.get();
            System.out.println(result);
          }
        }""";
    JavaFile expected = jFile(
        jClass("InferredPublic",
            jMethod("main", 2, 6, 68, 157)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
