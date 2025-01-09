package org.matwoess.jsourceprofiler.tool;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.common.JCompilerCommandBuilder;
import org.matwoess.jsourceprofiler.common.JavaCommandBuilder;
import org.matwoess.jsourceprofiler.common.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ProjectsTest.ExtensionContextParameterResolver.class)
public class ProjectsTest {
  private static final Path projectsRoot = Path.of("..", "projects");

  /**
   * Custom annotation to specify a GitHub repo needed for a project test.
   * The repository name should be specified in the "Owner/Name" format.
   * The destination folder is the name for the new folder inside projectsRoot.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @interface GithubRepoSource {
    String repositoryName();

    String destinationFolder();
  }

  /**
   * Detect our own GithubRepoSource annotation before executing Project tests
   * and automatically download it from GitHub if missing.
   *
   * @param context used for getting the method reference to the executing test
   */
  @BeforeEach
  public void downloadGithubRepoIfNecessary(ExtensionContext context) {
    Method testMethod = context.getRequiredTestMethod();
    if (testMethod.isAnnotationPresent(GithubRepoSource.class)) {
      GithubRepoSource annotation = testMethod.getAnnotation(GithubRepoSource.class);
      String localFolder = annotation.destinationFolder();
      String repositoryUrl = annotation.repositoryName();
      if (!projectsRoot.resolve(localFolder).toFile().exists()) {
        //noinspection ResultOfMethodCallIgnored
        projectsRoot.toFile().mkdirs();
        Path zip = TestUtils.downloadGithubRepoZip(projectsRoot, localFolder, repositoryUrl);
        if (zip != null) {
          TestUtils.unzipRepo(zip, projectsRoot, localFolder);
        }
      }
    }
  }

  /**
   * Instrumenting Coco/R using Coco/R, then calling Coco/R on Coco/R.
   * <p/>
   * The sources are first compiled without instrumentation to re-generate
   * the <code>Scanner.java</code> and <code>Parser.java</code> files.
   * <p/>
   * This is necessary because the copyright is removed in the process,
   * effectively changing the source file. The initial metadata would then not match anymore.
   */
  @Test
  @GithubRepoSource(repositoryName = "SSW-CocoR/CocoR-Java", destinationFolder = "CocoR")
  public void testCocoR() {
    Path sourcesRoot = projectsRoot.resolve("CocoR").resolve("src");
    Path cocoAtg = sourcesRoot.resolve("Coco.atg");
    // re-generate Parser and Scanner once before instrumentation
    // Coco removes the copyright notice when (re-)creating its own Parser and Scanner
    // leading to suddenly 27 lines less and mismatching metadata
    int firstCompileResult = Util.runCommand(new JCompilerCommandBuilder()
        .setDirectory(sourcesRoot)
        .addCompileArg("--release", "9")
        .addSourceFile(sourcesRoot.resolve("Trace.java"))
        .addSourceFile(sourcesRoot.resolve("Scanner.java"))
        .addSourceFile(sourcesRoot.resolve("Tab.java"))
        .addSourceFile(sourcesRoot.resolve("DFA.java"))
        .addSourceFile(sourcesRoot.resolve("ParserGen.java"))
        .addSourceFile(sourcesRoot.resolve("Parser.java"))
        .addSourceFile(sourcesRoot.resolve("Coco.java"))
        .build());
    assertEquals(0, firstCompileResult);
    int runWithoutInstrumentationResult = Util.runCommand(new JavaCommandBuilder()
        .setClassPath(sourcesRoot)
        .setMainClass("Coco/Coco")
        .addArgs(cocoAtg.toString())
        .build());
    assertEquals(0, runWithoutInstrumentationResult);
    // now we instrument the new version without copyright notices
    TestUtils.instrumentFolder(sourcesRoot);
    // manually compile, because we need the legacy "-source" and "-target" parameters with the Java 21 compiler
    Path instrDir = IO.getInstrumentDir();
    int compileInstrumentedResult = Util.runCommand(new JCompilerCommandBuilder()
        .setDirectory(instrDir)
        .setClassPath(instrDir)
        .addCompileArg("--release", "9")
        .addSourceFile(instrDir.resolve("Trace.java"))
        .addSourceFile(instrDir.resolve("Scanner.java"))
        .addSourceFile(instrDir.resolve("Tab.java"))
        .addSourceFile(instrDir.resolve("DFA.java"))
        .addSourceFile(instrDir.resolve("ParserGen.java"))
        .addSourceFile(instrDir.resolve("Parser.java"))
        .addSourceFile(instrDir.resolve("Coco.java"))
        .build());
    assertEquals(0, compileInstrumentedResult);
    // run Coco on Coco with the Coco ATG
    int runInstrumentedResult = Util.runCommand(new JavaCommandBuilder()
        .setClassPath(IO.getInstrumentDir())
        .setMainClass("Coco/Coco")
        .addArgs(cocoAtg.toString())
        .build());
    assertEquals(0, runInstrumentedResult);
    // manually generate report
    TestUtils.generateReport();
  }

  /**
   * Does not have a main file, but we create one for getting a valid report easily.
   */
  @Test
  @GithubRepoSource(repositoryName = "srikanth-lingala/zip4j", destinationFolder = "zip4j")
  public void testZip4j() throws IOException {
    Path sourcesRoot = projectsRoot.resolve(Path.of("zip4j", "src", "main", "java"));
    Path mainFile = sourcesRoot.resolve("Main.java");
    if (!mainFile.toFile().exists()) {
      Files.writeString(mainFile, """
          import net.lingala.zip4j.ZipFile;
          import java.io.IOException;
          import java.nio.file.Files;
          import java.nio.file.Path;
                    
          public class Main {
            public static void main(String[] args) {
              try (ZipFile zipFile = new ZipFile(Files.createTempFile(null, "zip").toFile())) {
                zipFile.addFolder(Path.of(".profiler", "instrumented").toFile());
              } catch (IOException e) {throw new RuntimeException(e);}
            }
          }
          """);
    }
    TestUtils.instrumentFolderAndProfile(sourcesRoot, "Main.java");
  }

  /**
   * Uses maven to build. Only included to test grammar stability.
   */
  @Test
  @GithubRepoSource(repositoryName = "jacoco/jacoco", destinationFolder = "jacoco")
  public void testJaCoCo_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("jacoco");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }

  /**
   * Uses ant to build. Only included to test grammar stability.
   */
  @Test
  @GithubRepoSource(repositoryName = "dacapobench/dacapobench", destinationFolder = "dacapobench")
  public void testDaCapoBench_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("dacapobench");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }


  /**
   * Uses gradle to build. Only included to test grammar stability.
   */
  @Test
  @GithubRepoSource(repositoryName = "junit-team/junit5", destinationFolder = "junit5")
  public void testJUnit5_instrumentAndMock() {
    Path sourcesRoot = projectsRoot.resolve("junit5");
    TestUtils.instrumentFolder(sourcesRoot);
    TestUtils.createMockCounterData();
    TestUtils.generateReport();
  }


  /**
   * Needed for JUnit to resolve ExtensionContext context in @BeforeEach methods
   */
  static class ExtensionContextParameterResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
      return parameterContext.getParameter().getType() == ExtensionContext.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
      return extensionContext;
    }
  }

}
