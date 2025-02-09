package org.matwoess.jsourceprofiler.tool.cli;

import org.matwoess.jsourceprofiler.common.RunMode;
import org.matwoess.jsourceprofiler.common.Util;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the command line arguments for the profiler tool.
 *
 * @param runMode       one of {@link RunMode}
 * @param targetPath    the path to the main Java file (or directory for instrument-only mode)
 * @param sourcesDir    the directory with additional Java files to instrument
 * @param syncCounters  whether to instrument using synchronized counter-increments
 * @param verboseOutput whether to output verbose info about instrumentation of files
 * @param programArgs   the program arguments to pass to the main method
 */
public record Arguments(
    RunMode runMode,
    Path targetPath,
    Path sourcesDir,
    boolean syncCounters,
    boolean verboseOutput,
    String[] programArgs) {

  /**
   * Parses the command line arguments and returns an {@link Arguments} object.
   *
   * @param args the tool's command line string arguments
   * @return the parsed arguments as a record
   * @throws IllegalArgumentException if insufficient, too many, or invalid arguments are given
   */
  public static Arguments parse(String[] args) throws IllegalArgumentException {
    if (args.length == 0) {
      throw new IllegalArgumentException("No arguments specified.");
    }
    RunMode runMode = RunMode.DEFAULT;
    boolean syncCounters = false;
    boolean verboseOutput = false;
    Path sourcesDir = null;
    int i = 0;
    for (; i < args.length; i++) {
      if (!args[i].startsWith("-")) {
        break;
      }
      switch (args[i]) {
        case "-h", "--help" -> {
          printUsage();
          return null;
        }
        case "-s", "--synchronized" -> syncCounters = true;
        case "-v", "--verbose" -> verboseOutput = true;
        case "-i", "--instrument-only" -> {
          if (runMode != RunMode.DEFAULT) {
            throw new IllegalArgumentException("Multiple run modes specified.");
          }
          runMode = RunMode.INSTRUMENT_ONLY;
        }
        case "-r", "--generate-report" -> {
          if (runMode != RunMode.DEFAULT) {
            throw new IllegalArgumentException("Multiple run modes specified.");
          }
          runMode = RunMode.REPORT_ONLY;
        }
        case "-d", "--sources-directory" -> {
          i++;
          if (i == args.length) { // no additional argument
            throw new IllegalArgumentException("No sources directory specified.");
          }
          sourcesDir = Path.of(args[i]);
          if (!sourcesDir.toFile().isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + sourcesDir.toAbsolutePath().normalize());
          }
        }
        default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
      }
    }
    String[] remainingArgs = Arrays.copyOfRange(args, i, args.length);
    Path targetPath = null;
    String[] programArgs = null;
    if (remainingArgs.length > 0) {
      targetPath = Path.of(remainingArgs[0]);
    }
    // additional validations
    switch (runMode) {
      case REPORT_ONLY -> {
        if (remainingArgs.length > 0) {
          throw new IllegalArgumentException("No arguments allowed for the report-only run mode.");
        }
      }
      case INSTRUMENT_ONLY -> {
        if (remainingArgs.length != 1) {
          throw new IllegalArgumentException("Exactly one argument required for the instrument-only run mode.");
        }
      }
      case DEFAULT -> {
        if (remainingArgs.length == 0) {
          throw new IllegalArgumentException("No main file specified.");
        }
        if (!Util.isJavaFile(targetPath)) {
          throw new IllegalArgumentException("Not a Java source file: " + targetPath.toAbsolutePath().normalize());
        }
        if (sourcesDir != null && !Util.isAncestorOf(sourcesDir, targetPath)) {
          throw new IllegalArgumentException("Main file must be located inside the sources directory or in any child directory.");
        }
        if (remainingArgs.length > 1) {
          programArgs = Arrays.copyOfRange(remainingArgs, 1, remainingArgs.length);
        }
      }
    }
    return new Arguments(runMode, targetPath, sourcesDir, syncCounters, verboseOutput, programArgs);
  }

  /**
   * Prints tool usage information to the standard output.
   */
  public static void printUsage() {
    System.out.println(getUsage());
  }

  /**
   * Returns the tool usage information as a string.
   *
   * @return the usage information
   */
  public static String getUsage() {
    return """
        Usage: profiler [options] <main file> [program args]
        Or   : profiler [options] <run mode>
        Options:
          -h, --help                        display this message and quit
          -s, --synchronized                instrument using synchronized counter increments
          -v, --verbose                     output verbose info about instrumentation of files
          -d, --sources-directory <dir>     directory with additional Java files to instrument
        Run mode (exclusive):
          -i, --instrument-only <file|dir>  only instrument a single file or directory and exit
          -r, --generate-report             only generate the report from metadata and counts
        Main file:
          The path to the main Java file. It will be compiled and and executed after instrumentation.
          (Must not be specified for the generate-report run mode)
        Program args:
          Will be passed to the main method if given
        """;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Arguments arguments = (Arguments) o;

    if (syncCounters != arguments.syncCounters) return false;
    if (verboseOutput != arguments.verboseOutput) return false;
    if (runMode != arguments.runMode) return false;
    if (!Objects.equals(targetPath, arguments.targetPath)) return false;
    if (!Objects.equals(sourcesDir, arguments.sourcesDir)) return false;
    return Arrays.equals(programArgs, arguments.programArgs);
  }

  @Override
  public int hashCode() {
    int result = runMode.hashCode();
    result = 31 * result + (targetPath != null ? targetPath.hashCode() : 0);
    result = 31 * result + (sourcesDir != null ? sourcesDir.hashCode() : 0);
    result = 31 * result + (syncCounters ? 1 : 0);
    result = 31 * result + (verboseOutput ? 1 : 0);
    result = 31 * result + Arrays.hashCode(programArgs);
    return result;
  }
}
