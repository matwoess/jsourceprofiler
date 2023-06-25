package instrument;

import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.TestProgramBuilder.*;
import static instrument.TestInstrumentUtils.parseJavaFile;
import static model.BlockType.*;
import static model.ClassType.ENUM;
import static model.ClassType.INTERFACE;

public class EnumsTest {
  @Test
  public void testEmptyEnum() {
    String fileContent = """
        enum Empty {
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "Empty", false)
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSimpleEnumWithoutMembers() {
    String fileContent = """
        enum Adjective {
          LOW, MEDIUM, HIGH,
          WEAK, STRONG, GREAT
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "Adjective", false)
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithTrailingSemicolon() {
    String fileContent = """
        enum AB {
          A, B;
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "AB", false)
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testStaticBlock() {
    String fileContent = """
        enum AB {
          A, B;
          public static final long ID;
          static {
            ID = 1237877L;
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "AB", false,
            jBlock(STATIC, 4, 6, 59, 82)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testMemberFunction() {
    String fileContent = """
        enum Enum {
          VALUE1, VAL_2 ;
          public static final long ID;
          public String lowercase() {
            return this.name().toLowerCase();
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "Enum", false,
            jMethod("lowercase", false, 4, 6, 90, 132)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithConstructorAndArguments() {
    String fileContent = """
        public enum WithConstructor {
          PRIMARY(1, "OK", 1.5f),
          SECONDARY(-6634, "", 5e+4f),
          TERTIARY(5, "This is a test", .3f);
         
          private final int integerVal;
          private final String stringVal;
          public final float floatVal;
         
          WithConstructor(int val1, String str, float num) {
            this.integerVal = val1;
            this.stringVal = str;
            this.floatVal = num;
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "WithConstructor", false,
            jMethod("WithConstructor",
                jBlock(CONSTRUCTOR, 10, 14, 276, 359)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumMainFunctionAndSwitch() {
    String fileContent = """
        public enum WithMain {
          A,
          B,
          C;
          public static void main(String[] args) {
            WithMain wm = A;
            switch (wm) {
              case A:
                assert (e == A);
              default:
                System.out.println(wm.name());
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "WithMain", true,
            jMethod("main", true, 5, 13, 80, 222,
                jBlock(SWITCH_CASE, 8, 9, 133, 158),
                jBlock(SWITCH_CASE, 10, 11, 173, 212)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithSubClassAndSubInterface() {
    String fileContent = """
        public enum WithSubClassAndInterface {
          A,B,C;
          
          static class ClassInEnum implements InterfaceInEnum {
            public static void printName(WithSubClassAndInterface val) {
              System.out.println(val.name());
            }
          }
          
          private interface InterfaceInEnum {
            default String lowercase(WithSubClassAndInterface val) {
              return val.name().toLowerCase();
            }
          }
          
          public static void callMethods() {
            ClassInEnum.printName(WithSubClassAndInterface.B);
            new ClassInEnum().lowercase(C);
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "WithSubClassAndInterface", false,
            jClass("ClassInEnum",
                jMethod("printName", false, 5, 7, 169, 213)
            ),
            jClass(INTERFACE, "InterfaceInEnum", false,
                jMethod("lowercase", false, 11, 13, 317, 362)
            ),
            jMethod("callMethods", false, 16, 19, 404, 499)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithInnerEnum_WithAndWithoutSemicolon() {
    String fileContent = """
        public enum WithInnerEnum {
          A,B,C;
           
          enum InnerEnum {
            D,E;
          }
           
          public static void printValues() {
            for (InnerEnum val : InnerEnum.values()) {
              System.out.println(val);
            }
            for (WithInnerEnum val : WithInnerEnum.values()) {
              System.out.println(val);
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "WithInnerEnum", false,
            jClass(ENUM, "InnerEnum", false),
            jMethod("printValues", false, 8, 15, 107, 287,
                jBlock(BLOCK, 9, 11, 154, 191),
                jBlock(BLOCK, 12, 14, 246, 283)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    fileContent = fileContent.replace("D,E;", "D,E");
    expected.foundBlocks.forEach(block -> {
      block.begPos--;
      block.incInsertPosition--;
      block.endPos--;
    });
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}