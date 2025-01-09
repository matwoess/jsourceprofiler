package org.matwoess.jsourceprofiler.tool.instrument;

import org.junit.jupiter.api.Test;
import org.matwoess.jsourceprofiler.tool.model.JavaFile;

import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.jBlock;
import static org.matwoess.jsourceprofiler.tool.model.BlockType.*;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.*;

public class SwitchPatternMatchingTest {
  @Test
  public void testInstanceOfPatternsWithGuards() {
    String fileContent = """
        sealed interface Shape {
          record Rectangle(double width, double height) implements Shape {}
          record Square(double side) implements Shape {}
          record Circle(double radius) implements Shape {}
        }
        class SwitchPatternMatching {
          public static void main(String[] args) {
            Shape[] shapes = {
                new Shape.Rectangle(2, 3),
                new Shape.Rectangle(3, 3),
                new Shape.Square(4),
                new Shape.Circle(5)
            };
            for (Shape shape : shapes) {
              printArea(shape);
            }
          }
          static void printArea(Shape shape) {
            double area = calculateArea(shape);
            System.out.println("Area: " + area);
          }
          static double calculateArea(Shape shape) {
            return switch (shape) {
              case Shape.Rectangle r when r.width() == r.height() -> Math.pow(r.width(), 2);
              case Shape.Rectangle r -> r.width() * r.height();
              case Shape.Square s -> {
                yield s.side() * s.side();
              }
              case Shape.Circle c when c.radius() < 0 -> throw new RuntimeException("invalid circle radius");
              case Shape.Circle c -> Math.PI * c.radius() * c.radius();
            };
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Shape",
            jClass("Rectangle"),
            jClass("Square"),
            jClass("Circle")
        ),
        jClass("SwitchPatternMatching",
            jMethod("main", 7, 17, 266, 491,
                jBlock(LOOP, 14, 16, 456, 487)
            ),
            jMethod("printArea", 18, 21, 529, 615),
            jMethod("calculateArea", 22, 32, 659, 1080,
                jBlock(SWITCH_EXPR, 23, 31, 687, 1075,
                    jSsBlock(ARROW_CASE, 24, 24, 749, 773),
                    jSsBlock(ARROW_CASE, 25, 25, 805, 829),
                    jBlock(ARROW_CASE, 26, 28, 859, 903).withControlBreak(YIELD),
                    jSsBlock(ARROW_CASE, 29, 29, 952, 1005).withControlBreak(THROW),
                    jSsBlock(ARROW_CASE, 30, 30, 1034, 1069)
                )
            ).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testRecordPatternsWithGuards() {
    String fileContent = """
        record Point<T>(T x, T y) {}
        sealed interface Unit permits IntCoord, FloatCoord {}
        record IntCoord(Integer val) implements Unit {}
        record FloatCoord(Float val) implements Unit {}
                
        class SwitchPatternMatching {
          public static void main(String[] args) {
            List<Point<Unit>> points = List.of(
              new Point<>(new IntCoord(0), new IntCoord(0)),
              new Point<>(new IntCoord(1), new IntCoord(2)),
              new Point<>(new FloatCoord(1.0f), new FloatCoord(27.02134325f)),
              new Point<>(new IntCoord(3), new FloatCoord(2.5f)),
              new Point<>(new FloatCoord(-3f), new IntCoord(25))
            );
            for (Point<Unit> point : points) {
              printGenericPoint(point);
            }
          }
          static void printGenericPoint(Point<Unit> p) {
            switch (p) {
              case Point<Unit>(IntCoord x, IntCoord y) when x.val() == 0 && y.val() == 0 -> System.out.println("origin");
              case Point(IntCoord x, IntCoord y) -> System.out.printf("iiPoint = (%d, %d)%n", x.val(), y.val());
              case Point(FloatCoord x, FloatCoord y) -> System.out.printf("ffPoint = (%.2f, %.2f)%n", x.val(), y.val());
              case Point(Unit x, IntCoord y) -> System.out.printf("xiPoint = (%s, %d)%n", x.toString(), y.val());
              case Point(Unit x, FloatCoord y) -> System.out.printf("xfPoint = (%s, %.2f)%n", x.toString(), y.val());
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Point"),
        jClass("Unit"),
        jClass("IntCoord"),
        jClass("FloatCoord"),
        jClass("SwitchPatternMatching",
            jMethod("main", 7, 18, 251, 672,
                jBlock(LOOP, 15, 17, 629, 668)
            ),
            jMethod("printGenericPoint", 19, 27, 720, 1296,
                jBlock(SWITCH_STMT, 20, 26, 737, 1292,
                    jSsBlock(ARROW_CASE, 21, 21, 822, 852),
                    jSsBlock(ARROW_CASE, 22, 22, 896, 957),
                    jSsBlock(ARROW_CASE, 23, 23, 1005, 1070),
                    jSsBlock(ARROW_CASE, 24, 24, 1110, 1176),
                    jSsBlock(ARROW_CASE, 25, 25, 1218, 1286)
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testColonCaseSwitchWithGenericPatternsAndGuards() {
    String fileContent = """
        record Point<T>(T x, T y) {}
        sealed interface Unit permits IntCoord, FloatCoord {}
        record IntCoord(Integer val) implements Unit {}
        record FloatCoord(Float val) implements Unit {}
                
        class SwitchPatternMatching {
          public static void main(String[] args) {
            printObject("some string");
            printObject(.55d);
            printObject(-60);
            printObject(-7);
            printObject(points.getFirst());
            printObject(null);
            printObject(new Point<>(new IntCoord(5), new IntCoord(5)));
          }
          private static void printObject(Object obj) {
            switch (obj) {
             case String s:
               System.out.println("String: " + s);
               break;
             case Integer i when i < 0 && i != Integer.MIN_VALUE && i % 2 != 0:
               if (i == -7) {
                 System.out.println("Special Negative Odd Integer: " + i);
                 break;
               }
               System.out.println("Negative Odd Integer: " + i);
               System.out.println("Absolute Value: " + Math.abs(i));
               break;
             case Integer i:
               System.out.println("Integer: " + i);
               break;
             case Float f: {
               System.out.println("Float: " + f);
               break;
             }
             case Double d:
               System.out.println("Double: " + d);
               break;
             case Point(IntCoord x, IntCoord y) when x.val() == 0 && y.val() == 0:
                 System.out.println("Origin Point");
                 break;
             case null, default:
               System.out.println("Unknown type");
               break;
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Point"),
        jClass("Unit"),
        jClass("IntCoord"),
        jClass("FloatCoord"),
        jClass("SwitchPatternMatching",
            jMethod("main", 7, 15, 251, 477),
            jMethod("printObject", 16, 46, 524, 1407,
                jBlock(SWITCH_STMT, 17, 45, 543, 1403,
                    jBlock(COLON_CASE, 18, 20, 564, 621).withControlBreak(BREAK).noIncOffset(),
                    jBlock(COLON_CASE, 21, 28, 693, 939,
                        jBlock(BLOCK, 22, 25, 714, 807).withControlBreak(BREAK)
                    ).withControlBreak(BREAK).noIncOffset(),
                    jBlock(COLON_CASE, 29, 31, 960, 1018).withControlBreak(BREAK).noIncOffset(),
                    jBlock(COLON_CASE, 32, 35, 1037, 1102,
                        jBlock(BLOCK, 32, 35, 1038, 1102).withControlBreak(BREAK)
                    ).noIncOffset(),
                    jBlock(COLON_CASE, 36, 38, 1122, 1179).withControlBreak(BREAK).noIncOffset(),
                    jBlock(COLON_CASE, 39, 41, 1254, 1315).withControlBreak(BREAK).noIncOffset(),
                    jBlock(COLON_CASE, 42, 44, 1340, 1397).withControlBreak(BREAK).noIncOffset()
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
