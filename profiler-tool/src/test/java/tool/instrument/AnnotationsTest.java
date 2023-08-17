package tool.instrument;

import org.junit.jupiter.api.Test;
import tool.model.JavaFile;

import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.instrument.TestProgramBuilder.*;
import static tool.model.BlockType.BLOCK;

public class AnnotationsTest {
  @Test
  public void testClassAnnotations() {
    String fileContent = """
        @SuppressWarnings("unchecked")
        @Deprecated
        public class Annotations {
          public static void main(String[] args) {
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations",
            jMethod("main", 4, 5, 112, 116)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMethodAnnotations() {
    String fileContent = """
        public class Annotations {
          @Deprecated
          @SuppressWarnings({"unused", "unchecked"})
          public static void main(String[] args) {
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations",
            jMethod("main", 4, 5, 128, 132)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testArgumentAnnotations() {
    String fileContent = """
        public class Annotations {
          public static void main(@SuppressWarnings("null") String[] args) {
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations",
            jMethod("main", 2, 3, 95, 99)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testStatementAnnotations() {
    String fileContent = """
        public class Annotations {
          public static void main(String[] args) {
            @SuppressWarnings("ParseError")
            Integer i1 = Integer.parseInt("000555");
            @SuppressWarnings("unused")
            String s = "a";
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations",
            jMethod("main", 2, 7, 69, 206)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSimpleOuterAnnotationType() {
    String fileContent = """
        @interface VersionID {
          long id();
        }
                
        @SuppressWarnings({"unused"})
        @VersionID(id = 5123L)
        public class Annotations {
          public static void main(String[] args) {
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations",
            jMethod("main", 8, 9, 161, 165)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testComplexInnerAnnotationTypeWithDefaultsAndQualifiedName() {
    String fileContent = """
        package complexAnnotations;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
                
        public class Annotations {
          
          private static class RuntimeRetentionPolicy {
            @Documented
            @Retention(RetentionPolicy.RUNTIME)
            @interface AuthorMetadata {
              String author();
              String date();
              int revision() default 1;
              String[] comments() default {"no comment"};
            }
          }
          @Override
          @SuppressWarnings({"unused", "unchecked"})
          @RuntimeRetentionPolicy.AuthorMetadata(
              author = "Myself",
              date = "01.03.2020",
              comments = {"Important!", "Is documented\\n"}
          )
          public boolean equals(@SuppressWarnings("null") Object obj) {
            return super.equals(obj);
          }
          public static void main(String[] args) {
          }
        }
        """;
    JavaFile expected = jFile("complexAnnotations", 27,
        jClass("Annotations",
            jClass("RuntimeRetentionPolicy"),
            jMethod("equals", 24, 26, 682, 716),
            jMethod("main", 27, 28, 759, 763)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testAnnotationWithTextBlockParameter() {
    String fileContent = """
        class AnnotationTests {
          @ParameterizedTest
          @CsvSource(delimiter = '|', nullValues = "n/a", textBlock = ""\"
              foo,bar,baz | baz,bar,foo
              1,2         | 2,1
              n/a         | n/a
              ""\")
          void bothNullOrReversed(String input, String expected) {
            if (expected == null) {
              assertNull(input);
            } else {
              List<String> items = Arrays.stream(input.split(",")).collect(Collectors.toList());
              ;
              Collections.reverse(items);
              String result = String.join(",", items);
              assertEquals(expected, result);
            }
          }
        }
        """;
    System.out.println(getBuilderCode(parseJavaFile(fileContent)));
    JavaFile expected = jFile(
        jClass("AnnotationTests",
            jMethod("bothNullOrReversed", 8, 18, 260, 552,
                jBlock(BLOCK, 9, 11, 288, 319),
                jBlock(BLOCK, 11, 17, 326, 548)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
