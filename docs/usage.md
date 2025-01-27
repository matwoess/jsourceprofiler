# Usage

The tool defines the following command line interface:

```
Usage: profiler [options] <main file> [program args]  
Or   : profiler [options] <run mode>
```
The `java` and `javac` JDK binaries have to be included in the system environment path for the tool to function correctly.
The absolute file system location of the executables is not determined automatically. 
They are invoked with their respective names in a sub-process.

All tool output is stored inside the hidden `.profiler` subdirectory of the current working directory. 
It is created automatically if it does not exist.

For being able to use local project resources and relative paths (as arguments or inside the program itself)
the tool never changes its working directory during execution. Instead, the classpath is set to the instrumented copies
inside the `.profiler` directory.
It is therefore recommended to run the command line tool in the project's root directory.

In the simplest case the tool can be invoked with a single Main file as its target.
<br/>
If the program-to-profile consists of multiple class source files the `--sources-directory` (`-d`) parameter is **required**!
Setting this parameter instructs the tool to parse all Java files in the given directory and its subdirectories.

## Shell script

For frequent use, it is recommended to create a shell script or batch file that contains the necessary commands.
```shell title="~/bin/profile"
#!/usr/bin/env bash
java -jar ~/.local/lib/profiler-0.12.0.jar $@
# java -> /usr/lib/jvm/java-21-openjdk/bin/java
```

This script can then be placed in a directory that is included in the system's PATH variable.
It allows the tool to be called from any location in the terminal by its file name (e.g. `profile`).

(In the rest of this file the `java -jar profiler.jar` command is substituted by `profile`)

## Command line options
There are a few optional arguments available. For a full list, see the `-h` or `--help` arguments.

### `--sources-directory`
If the project-to-profile consists of two or more (linked) Java files, the sources directory has to be specified.
This is done with the `-d` or `--sources-directory` option:
```shell
profile -d src/main/java/ src/main/java/subfolder/Main.java
```

Using this option, all `.java` files inside `src/main/java/` will be parsed, instrumented and copied to the
`.profiler/instrumented` directory. The relative folder-structure is replicated to ensure successful compilation 
(`javac` will throw an error if the package name and file paths mismatch).

### `--synchronized`
When adding `-s` or `--synchronized` as a option, all inserted counters will be incremented atomically.
This might be useful for multi-threaded programs, where a few methods or blocks are constantly executed in parallel.
It will ensure that hit counts are correct, but runtime performance will be impacted.

### `--verbose`
This option is mainly for debugging purposes. It can be activated with `-v` or `--verbose` and will output
detailed information about the parsing process for each file.

## Run modes

The tool is primarily designed for easy usage with small projects that have a Main file.
In case the project cannot be compiled with `javac Main.java`, or uses build tools (like Maven, Gradle, or Ant),
we cannot use the default compilation logic.

For this case, two additional run modes are available:

### `--instrument-only`

By specifying the `-i <file|dir>` or `--instrument-only <file|dir>` mode the target file (or directory
with all its Java files) will be instrumented and written to the `.profiler/instrumented/` directory.
The `metadata.dat` file will also be generated.
The instrumented code can then be compiled by *custom* commands and run manually.

### `--generate-report`

If a project was already instrumented and run, the HTML report can be quickly (re-)generated
with the `-r` or `--generate-report` run mode.
In this mode, no parsing or instrumentation will be done.
For it to succeed the `metadata.dat` and `counts.dat` files must already exist in the `.profiler` output directory.


## Sample usage

### Single file projects

If the entire program-to-profile is contained in a *single* Java source file, the tool can be used as following:
```shell
profile Main.java arg1 arg2 ...
```
This will parse, instrument and compile the given file, execute the program with the given arguments, and create a report.

### Multi-file projects

If the program consists of multiple Java files, the sources directory has to be specified:

```shell
profile -d src/ src/Main.java arg1 arg2 ...
```

This will parse all `*.java` files in the sources directory and create an instrumented copy.
The first positional parameter to the tool specifies the class containing the main entry point. 
Its instrumented copy will be used to run the program.

### Run modes and custom compilation
In case the project cannot be compiled with `javac Main.java`, the two additional run modes can be used:

```shell { hl_lines="2 3" linenums="1" }
profile --instrument-only src
javac -cp .profiler/instrumented -customArg .profiler/instrumented/Main.java
java -cp .profiler/instrumented Main
profile --generate-report
```
This way the `-customArg` can be passed to the `javac` command. 
Instead of lines 2-3 any other custom command like `ant`/`gradlew`/.. can be substituted.
