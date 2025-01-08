package org.matwoess.jsourceprofiler.tool.instrument;

import org.matwoess.jsourceprofiler.tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static org.matwoess.jsourceprofiler.tool.instrument.TestInstrumentUtils.parseJavaFile;
import static org.matwoess.jsourceprofiler.tool.instrument.TestProgramBuilder.*;
import static org.matwoess.jsourceprofiler.tool.model.BlockType.*;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.RETURN;
import static org.matwoess.jsourceprofiler.tool.model.ControlBreak.Kind.THROW;

public class RecordsTest {
  @Test
  void testRecordWith1FieldAndNoBody() {
    String fileContent = """
        record Record(int x) {}
        """;
    JavaFile expected = jFile(
        jClass("Record")
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testRecordWithCanonicalConstructor() {
    String fileContent = """
        record LenWidth(double length, double width) {
          LenWidth(double length, double width) {
            if (length < 0 || width < 0) {
              throw new IllegalArgumentException(String.format("Invalid dimensions: %f, %f", length, width));
            }
            this.length = length;
            this.width = width;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("LenWidth",
            jConstructor("LenWidth", 2, 8, 87, 285,
                jBlock(BLOCK, 3, 5, 122, 231).withControlBreak(THROW)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testRecordWithCanonicalConstructorsAndCompactForm() {
    String fileContent = """
        record LenWidth(double length, double width) {
          LenWidth {
            if (length < 0 || width < 0) {
              throw new IllegalArgumentException(String.format("Invalid dimensions: %f, %f", length, width));
            }
          }
          LenWidth(String lenStr, String widthStr) {
            this(Double.parseDouble(lenStr), Double.parseDouble(widthStr));
          }
        }
        """;
    int thisCallOffset = "{\n    this(Double.parseDouble(lenStr), Double.parseDouble(widthStr));".length();
    JavaFile expected = jFile(null, 0,
        jClass("LenWidth",
            jConstructor("LenWidth", 2, 6, 58, 206,
                jBlock(BLOCK, 3, 5, 93, 202).withControlBreak(THROW)
            ),
            jConstructor("LenWidth", 7, 9, 250, 323).incOffset(thisCallOffset)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testRecordWithCustomFieldAccess() {
    String fileContent = """
        record Coordinate(int x, int y) {
          @Override
          public int x() {
            System.out.println("x was accessed with a value of: " + x);
            return x;
          }
          @Override
          public int y() {
            System.out.println("y was accessed with a value of: " + y);
            return x;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Coordinate",
            jMethod("x", 3, 6, 63, 146).withControlBreak(RETURN),
            jMethod("y", 8, 11, 176, 259).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testRecordWithStaticBlockMainMethodAndFunction() {
    String fileContent = """
        package recordPackage;
        public record Coordinate(int x, int y) {
          static int z;
          static {
            z = 50;
          }
          public static void main(String[] args) {
            Coordinate coord = new Coordinate(1, 5);
            coord.x();
            System.out.println(coord.y + z);
            System.out.println(coord.manhattanDistanceTo(new Coordinate(4, -1)));
          }
          int manhattanDistanceTo(Coordinate other) {
            return Math.abs(x - other.x()) + Math.abs(y - other.y());
          }
        }
        """;
    JavaFile expected = jFile("recordPackage", 22,
        jClass("Coordinate",
            jBlock(STATIC, 4, 6, 89, 106),
            jMethod("main", 7, 12, 148, 324),
            jMethod("manhattanDistanceTo", 13, 15, 369, 436).withControlBreak(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testNonClassRecordDefinitions() {
    String fileContent = """
        record Record() {
          private static final List<String> record = new ArrayList<>();
          public void record(String record) {
            this.record.add(record);
          }
          public String getRecord() {
            return record.get(0);
          }
          public static void main(String[] args) {
            Record record = new Record();
            Record.record.add("record");
            record.record("record");
            System.out.println(record.getRecord());
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Record",
            jMethod("record", 3, 5, 118, 152),
            jMethod("getRecord", 6, 8, 181, 212).withControlBreak(RETURN),
            jMethod("main", 9, 14, 254, 399)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
