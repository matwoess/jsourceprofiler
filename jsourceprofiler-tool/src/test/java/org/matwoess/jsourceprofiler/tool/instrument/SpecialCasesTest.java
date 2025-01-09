package org.matwoess.jsourceprofiler.tool.instrument;

import org.matwoess.jsourceprofiler.tool.model.JClass;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.model.BlockType.*;
import static org.matwoess.jsourceprofiler.tool.model.ClassType.ANONYMOUS;
import static org.matwoess.jsourceprofiler.tool.model.ClassType.LOCAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.RETURN;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.YIELD;

public class SpecialCasesTest {
  @Test
  public void testBeginOfImportsAndPackageName_NoPackage() {
    String fileContent = """
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
        }""";
    JavaFile expected = jFile(null, 0, jClass("Empty"));
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testBeginOfImportsAndPackageName() {
    String fileContent = """
        package name.Of._the_.pkg ;
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
          void meth() {
          }
          class InEmpty {
            void innerMeth() {
            }
          }
        }""";
    int lengthOfPackageDeclaration = "package name.Of._the_.pkg ;".length();
    JavaFile expected = jFile("name.Of._the_.pkg", lengthOfPackageDeclaration,
        jClass("Empty",
            jMethod("meth", 5, 6, 121, 126),
            jClass("InEmpty",
                jMethod("innerMeth", 8, 9, 166, 173)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    JClass tlClass = expected.topLevelClasses.get(0);
    JClass innerClass = expected.topLevelClasses.get(0).innerClasses.get(0);
    assertEquals("name.Of._the_.pkg.Empty", tlClass.getFullName());
    assertEquals("Empty", tlClass.getName());
    assertEquals("name.Of._the_.pkg.Empty$InEmpty", innerClass.getFullName());
    assertEquals("Empty$InEmpty", innerClass.getName());
  }

  @Test
  public void testNestedBlockTypes() {
    String fileContent = """
        class BlockTypeClass {
          static int a;
           
          static {
            if (5 % 2 == 1) switch ("this") {
              case "thus" -> a = 1;
              case "this" -> {
                a = 2;
              }
            }
            else {
              a = "this".chars().map(i -> i + 1).sum();
            }
          }
           
          public BlockTypeClass() {
            super();
            if (a == 2) {
              System.out.println("2");
            } else System.out.println("other");
          }
           
          public void method() {
            String x = "12345";
            if (x != null) {
              for (int charVal : x.chars().toArray())
                switch (charVal) {
                  case 1:
                    System.out.println(1);
                  case 2:
                    x.chars().map(ch -> {
                      return switch (ch) {
                        case 0 -> {
                          yield 0;
                        }
                        case 1 -> 1 + 5;
                        case 2 -> x.chars().map(c -> c + 1).filter(c -> {
                          return c < 10;
                        }).sum();
                        case 3 -> {
                          try {
                            if (ch == 3) yield 3;
                            else yield -1;
                          }
                          catch (Exception ignore) {}
                          finally {
                            System.out.println("leaving try");
                          }
                        }
                        case 4 -> {
                          yield switch (ch) {
                            default -> ch;
                          };
                        }
                        default -> -1;
                      };
                    }).forEach(i -> System.out.println(i));
                  case 3:
                    List<Integer> numbers = List.of(1, 2, 3, 4);
                    numbers.sort(new Comparator<Integer>() {
                      @Override
                      public int compare(Integer i1, Integer i2) {
                        int x = switch (i1) {
                          case 0 -> 0;
                          case -1 -> {
                            yield -1;
                          }
                          default -> i1 - i2;
                        };
                        return x;
                      }
                    });
                    if (numbers.size() > 5) System.out.println(numbers.get(0));
                  default:
                    System.out.println("other value");
                }
            }
            if (x.equals("abc")) {
              ;
            }
          }
        }
        """;
    JavaFile expected = jFile(null, 0,
        jClass("BlockTypeClass",
            jBlock(STATIC, 4, 14, 49, 237,
                jSsBlock(BLOCK, 5, 10, 70, 168,
                    jBlock(SWITCH_STMT, 5, 10, 87, 168,
                        jSsBlock(ARROW_CASE, 6, 6, 109, 116),
                        jBlock(ARROW_CASE, 7, 9, 138, 162)
                    )
                ),
                jBlock(BLOCK, 11, 13, 178, 233,
                    jSsBlock(LAMBDA, 12, 12, 213, 219)
                )
            ),
            jConstructor("BlockTypeClass", 16, 21, 265, 372,
                jBlock(BLOCK, 18, 20, 296, 334),
                jSsBlock(BLOCK, 20, 20, 339, 368)
            ).incOffset("{\n    super();".length()),
            jClass(ANONYMOUS, null,
                jMethod("compare", 62, 71, 1653, 1903,
                    jBlock(SWITCH_EXPR, 63, 69, 1691, 1860,
                        jSsBlock(ARROW_CASE, 64, 64, 1720, 1723),
                        jBlock(ARROW_CASE, 65, 67, 1753, 1804).withControlBreak(YIELD),
                        jSsBlock(ARROW_CASE, 68, 68, 1833, 1842)
                    )
                ).withControlBreak(RETURN)
            ),
            jMethod("method", 23, 81, 397, 2118,
                jBlock(BLOCK, 25, 77, 442, 2073,
                    jSsBlock(LOOP, 26, 76, 489, 2067,
                        jBlock(SWITCH_STMT, 27, 76, 515, 2067,
                            jBlock(COLON_CASE, 28, 29, 534, 569).noIncOffset(),
                            jBlock(COLON_CASE, 30, 57, 587, 1443,
                                jBlock(LAMBDA, 31, 57, 620, 1405,
                                    jBlock(SWITCH_EXPR, 32, 56, 655, 1390,
                                        jBlock(ARROW_CASE, 33, 35, 683, 729).withControlBreak(YIELD),
                                        jSsBlock(ARROW_CASE, 36, 36, 755, 762),
                                        jSsBlock(ARROW_CASE, 37, 39, 788, 887),
                                        // TODO: missing lambda blocks!!
                                        jBlock(ARROW_CASE, 40, 49, 914, 1203,
                                            jBlock(TRY, 41, 44, 938, 1036,
                                                jSsBlock(BLOCK, 42, 42, 972, 981).withControlBreak(YIELD),
                                                jSsBlock(BLOCK, 43, 43, 1006, 1016).withControlBreak(YIELD)
                                            ),
                                            jBlock(BLOCK, 45, 45, 1080, 1082),
                                            jBlock(BLOCK, 46, 48, 1109, 1185)
                                        ),
                                        jBlock(ARROW_CASE, 50, 54, 1230, 1343,
                                            jBlock(SWITCH_EXPR, 51, 53, 1268, 1324,
                                                jSsBlock(ARROW_CASE, 52, 52, 1300, 1304)
                                            )
                                        ).withControlBreak(YIELD),
                                        jSsBlock(ARROW_CASE, 55, 55, 1370, 1374)
                                    )
                                ).withControlBreak(RETURN),
                                jSsBlock(LAMBDA, 57, 57, 1419, 1441)
                            ).noIncOffset(),
                            jBlock(COLON_CASE, 58, 73, 1461, 1991,
                                jSsBlock(BLOCK, 73, 73, 1955, 1991)
                            ).noIncOffset(),
                            jBlock(COLON_CASE, 74, 75, 2010, 2057).noIncOffset()
                        )
                    )
                ),
                jBlock(BLOCK, 78, 80, 2099, 2114)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testNamingWithInnerAndLocalClasses() {
    String fileContent = """
        package mixed.pkg;
                
        class MixedClasses {
          public class Inner {
          }
            
          public static void main(String[] args) {
            Runnable r1 = new Runnable() {
              @Override
              public void run() {
              }
            };
            class Local1 {
            }
            Runnable r2 = new Runnable() {
              @Override
              public void run() {
                class Local2 {
                }
              }
            };
            interface Local3 {
            }
          }
            
          private Runnable r3 = new Runnable() {
            @Override
            public void run() {
            }
          };
        }""";
    JavaFile expected = jFile("mixed.pkg", 18,
        jClass("MixedClasses",
            jClass("Inner"),
            jClass(ANONYMOUS, null,
                jMethod("run", 10, 11, 187, 196)
            ),
            jClass(LOCAL, "Local1"),
            jClass(ANONYMOUS, null,
                jClass(LOCAL, "Local2"),
                jMethod("run", 17, 20, 304, 346)
            ),
            jClass(LOCAL, "Local3"),
            jClass(ANONYMOUS, null,
                jMethod("run", 28, 29, 465, 472)
            ),
            jMethod("main", 7, 24, 110, 386)
        )
    );
    JavaFile actual = parseJavaFile(fileContent);
    TestInstrumentUtils.assertResultEquals(expected, actual);
    List<JClass> innerClasses = expected.topLevelClasses.get(0).innerClasses;
    assertEquals("MixedClasses$Inner", innerClasses.get(0).getName());
    assertEquals("mixed.pkg.MixedClasses$Inner", innerClasses.get(0).getFullName());
    assertEquals("1", innerClasses.get(1).name);
    assertEquals("MixedClasses$1", innerClasses.get(1).getName());
    assertEquals("mixed.pkg.MixedClasses$1", innerClasses.get(1).getFullName());
    assertEquals("Local1", innerClasses.get(2).name);
    assertEquals("MixedClasses$Local1", innerClasses.get(2).getName());
    assertEquals("mixed.pkg.MixedClasses$Local1", innerClasses.get(2).getFullName());
    assertEquals("2", innerClasses.get(3).name);
    assertEquals("MixedClasses$2", innerClasses.get(3).getName());
    assertEquals("mixed.pkg.MixedClasses$2", innerClasses.get(3).getFullName());
    assertEquals("Local2", innerClasses.get(3).innerClasses.get(0).name);
    assertEquals("MixedClasses$2$Local2", innerClasses.get(3).innerClasses.get(0).getName());
    assertEquals("mixed.pkg.MixedClasses$2$Local2", innerClasses.get(3).innerClasses.get(0).getFullName());
    assertEquals("Local3", innerClasses.get(4).name);
    assertEquals("MixedClasses$Local3", innerClasses.get(4).getName());
    assertEquals("mixed.pkg.MixedClasses$Local3", innerClasses.get(4).getFullName());
    assertEquals("3", innerClasses.get(5).name);
    assertEquals("MixedClasses$3", innerClasses.get(5).getName());
    assertEquals("mixed.pkg.MixedClasses$3", innerClasses.get(5).getFullName());
  }

  @Test
  public void testNamesContainingDotClass() {
    String fileContent = """
        package dev.matwoess.classes;
        class DotClass {
          List<String> classes;
          public void addClass(String className) {
            this.classes.add("Class.class");
            this.classes.add(DotClass.class.getName());
            if (classes.getClass().isAnonymousClass()) {
              classes.toArray(String[]::new);
            }
          }
        }
        """;
    JavaFile expected = jFile("dev.matwoess.classes", 29,
        jClass("DotClass",
            jMethod("addClass", 4, 10, 112, 295,
                jBlock(BLOCK, 7, 9, 246, 291)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
