package org.matwoess.jsourceprofiler.tool.instrument;

import org.junit.jupiter.api.Test;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;

import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.model.BlockType.*;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.*;

public class SwitchesTest {

  @Test
  public void testSwitchWithExtraBlockBraces() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 1;
            switch (x) {
              case 1: {
                x += 3;
                break;
              }
              case 2: {}
              case 3: {
               x *= 2;
               x = x - 1;
              }
              case 4: {
                break;
              }
              default: { break; }
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 19, 61, 298,
                jBlock(SWITCH_STMT, 4, 18, 93, 294,
                    jBlock(COLON_CASE, 5, 8, 108, 149,
                        jBlock(BLOCK, 5, 8, 109, 149).withControlBreak(BREAK)
                    ).noIncOffset(),
                    jBlock(COLON_CASE, 9, 9, 163, 166,
                        jBlock(BLOCK, 9, 9, 164, 166)
                    ).noIncOffset(),
                    jBlock(COLON_CASE, 10, 13, 180, 223,
                        jBlock(BLOCK, 10, 13, 181, 223)
                    ).noIncOffset(),
                    jBlock(COLON_CASE, 14, 16, 237, 262,
                        jBlock(BLOCK, 14, 16, 238, 262).withControlBreak(BREAK)
                    ).noIncOffset(),
                    jBlock(COLON_CASE, 17, 17, 277, 288,
                        jBlock(BLOCK, 17, 17, 278, 288).withControlBreak(BREAK)
                    ).noIncOffset()
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testCommaSeparatedCases() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 1;
            switch (x) {
              case 1, 2, 3: {
                x += 3;
                break;
              }
              case 4, 5: {
                x *= 2;
                x = x - 1;
              }
              default: { break; }
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 15, 61, 253,
                jBlock(SWITCH_STMT, 4, 14, 93, 249,
                    jBlock(COLON_CASE, 5, 8, 114, 155,
                        jBlock(BLOCK, 5, 8, 115, 155).withControlBreak(BREAK)
                    ).noIncOffset(),
                    jBlock(COLON_CASE, 9, 12, 172, 217,
                        jBlock(BLOCK, 9, 12, 173, 217)
                    ).noIncOffset(),
                    jBlock(COLON_CASE, 13, 13, 232, 243,
                        jBlock(BLOCK, 13, 13, 233, 243).withControlBreak(BREAK)
                    ).noIncOffset()
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testNewSwitch() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            String value = "aBcDe";
            switch (value) {
              case "aBcDe" -> {
                if (value.toUpperCase().equals("ABCDE")) {
                  System.out.println("as expected");
                }
              }
              case "other value" -> System.out.println("unexpected");
              case "" -> {
                break;
              }
              default -> throw new RuntimeException("should never happen");
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 16, 61, 431,
                jBlock(SWITCH_STMT, 4, 15, 110, 427,
                    jBlock(ARROW_CASE, 5, 9, 134, 249,
                        jBlock(BLOCK, 6, 8, 185, 241)
                    ),
                    jSsBlock(ARROW_CASE, 10, 10, 277, 311),
                    jBlock(ARROW_CASE, 11, 13, 329, 353).withControlBreak(BREAK),
                    jSsBlock(ARROW_CASE, 14, 14, 370, 421).withControlBreak(THROW)
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSwitchExpression() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            for (int i = 0; i < 11; i++) {
              int result = switch (i) {
                case 5: {
                  yield 2;
                }
                case 1: case 2: yield 5;
                case 11: case 12: throw new RuntimeException();
                default: {
                  if (i < 10) {
                    System.out.println("none of the above");
                    yield 0;
                  } else {
                    yield -1;
                  }
                }
              };
              System.out.println("result=" + result);
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 21, 61, 510,
                jBlock(LOOP, 3, 20, 96, 506,
                    jBlock(SWITCH_EXPR, 4, 18, 128, 453,
                        jBlock(COLON_CASE, 5, 7, 145, 176,
                            jBlock(BLOCK, 5, 7, 146, 176).withControlBreak(YIELD)
                        ).noIncOffset(),
                        jBlock(COLON_CASE, 8, 8, 200, 209).withControlBreak(YIELD).noIncOffset(),
                        jBlock(COLON_CASE, 9, 9, 235, 265).withControlBreak(THROW).noIncOffset(),
                        jBlock(COLON_CASE, 10, 17, 282, 445,
                            jBlock(BLOCK, 10, 17, 283, 445,
                                jBlock(BLOCK, 11, 14, 307, 394).withControlBreak(YIELD),
                                jBlock(BLOCK, 14, 16, 400, 435).withControlBreak(YIELD)
                            )
                        ).noIncOffset()
                    )
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSwitchExpression_ArrowCases() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int dependingOn = 7;
            int result = switch (dependingOn) {
              case 5 -> {
                yield 2;
              }
              case 1, 2 -> 5;
              default -> {
                if (dependingOn < 10) {
                  System.out.println("none of the above");
                  yield 0;
                } else {
                  yield -1;
                }
              }
            };
            System.out.println("result=" + result);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 19, 61, 423,
                jBlock(SWITCH_EXPR, 4, 17, 126, 374,
                    jBlock(ARROW_CASE, 5, 7, 144, 170).withControlBreak(YIELD),
                    jSsBlock(ARROW_CASE, 8, 8, 189, 192),
                    jBlock(ARROW_CASE, 9, 16, 210, 368,
                        jBlock(BLOCK, 10, 13, 242, 323).withControlBreak(YIELD),
                        jBlock(BLOCK, 13, 15, 329, 360).withControlBreak(YIELD)
                    )
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testClassLevelSwitchExpression() {
    String fileContent = """
        public class ClassLevelSwitch {
          static int globalInt = switch ("switch".hashCode()) {
            case 12345 -> 5;
            case 6789 -> 6;
            default -> {
              yield 8;
            }
          };
                
          public static void main(String[] args) {
            System.out.println(globalInt);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("ClassLevelSwitch",
            jBlock(SWITCH_EXPR, 2, 8, 86, 170,
                jSsBlock(ARROW_CASE, 3, 3, 105, 108),
                jSsBlock(ARROW_CASE, 4, 4, 125, 128),
                jBlock(ARROW_CASE, 5, 7, 144, 166).withControlBreak(YIELD)
            ),
            jMethod("main", 10, 12, 214, 254)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testSwitchExpressionAsReturn() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            System.out.println(StatusCode.getStatusCodeDescription(StatusCode.FORBIDDEN));
            System.out.println(StatusCode.getStatusCodeDescription(StatusCode.UNAUTHORIZED));
          }
          static enum StatusCode {
            OK, UNAUTHORIZED, FORBIDDEN, NOTFOUND;
            public static String getStatusCodeDescription(StatusCode code) {
              return switch(code) {
                case OK -> "everything went great";
                case NOTFOUND, FORBIDDEN -> "cannot access";
                case UNAUTHORIZED -> {
                  yield "did you forget to enter your password?";
                }
              };
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jClass("StatusCode",
                jMethod("getStatusCodeDescription", 8, 16, 373, 613,
                    jBlock(SWITCH_EXPR, 9, 15, 401, 606,
                        jSsBlock(ARROW_CASE, 10, 10, 421, 446),
                        jSsBlock(ARROW_CASE, 11, 11, 482, 499),
                        jBlock(ARROW_CASE, 12, 14, 529, 598).withControlBreak(YIELD)
                    )
                ).withControlBreak(RETURN)
            ),
            jMethod("main", 2, 5, 61, 235)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

}
