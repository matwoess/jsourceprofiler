# Usage
```
Usage: profiler [options] <main file> [program args]  
Or   : profiler [options] <run mode>
```
The `java` and `javac` JDK binaries have to be included in the system environment path for the tool to function correctly.
The location of the executables is not determined automatically. They are invoked with their respective names.

All output is stored in the hidden `.profiler` subdirectory of the current working directory. 
It is created if it does not exist.

For being able to use local project resources and relative paths (as arguments or inside the program itself)
the tool never changes its working directory during execution. Instead, the classpath is set to the instrumented copies
inside the `.profiler` directory.
It is therefore recommended to run the command line tool in the project's root directory.

In the simplest case the tool can be invoked with a single Main file as its target.
<br/>
If the program-to-profile consists of multiple class source files the `--sources-directory` (`-d`) parameter is **required**!
Setting this parameter tells the tool to parse all Java files in the given directory and its subdirectories.


## Sample usage and explanation
(In the below section the `java -jar profiler.jar` command is substituted by `profiler`)

If the entire program is contained in a single Java file, the tool can be used as following:
```shell
profiler Main.java arg1 arg2 ...
```
This will parse the given file and create an instrumented copy in the `.profiler/instrumented/` folder.
The first argument to the tool specifies the class containing the main entry point.
<br/>
Additionally the `.profiler/metadata.dat` file will be created, containing information about
every found code block like its begin/end position, its parent method/class and other relevant data to create a report.

The tool will then automatically compile the instrumented version using `javac`.
```shell
javac -cp .profiler/instrumented -d .profiler/classes Main.java
```
The Java compiler itself finds referenced Java files (used in Main) and will compile them also
into (instrumented) `.class` files.<br/>
The compiled classes can then be found in the `.profiler/classes/` directory.

Next, the `java` binary will be used to execute the specified class by name (without the `.java` extension)
with the given arguments:
```
java -cp .profiler/classes Main arg1 arg2 ...
```
Executing the instrumented files, automatically stores the hit-counter values in `.profiler/counts.dat`
as soon as the program ends.

Finally, the metadata and counts will be used to create the report inside `.profiler/report/`.

## Command line options
There are a few optional arguments available. For a full list, see the `-h` or `--help` arguments.

### sources-directory
If the project-to-profile consists of two or more (linked) Java files, the sources directory has to be specified.
This is done with the `-d` or `--sources-directory` option:
```shell
profiler -d src/main/java/ src/main/java/subfolder/Main.java
```

Using this option, all `.java` files inside `src/main/java/` will be parsed, instrumented and copied to the
"instrumented" directory. The relative folder-structure is replicated to ensure successful compilation (`javac` will
throw an error if the package name and file paths mismatch).

### synchronized
When adding `-s` or `--synchronized` as a option, all inserted counters will be incremented atomically.
This might be useful for multi-threaded programs, where a few methods or blocks are constantly executed in parallel.
It will ensure that hit counts are correct, but runtime performance will be impacted.

### verbose
This option is mainly for debugging purposes. It can be activated with `-v` or `--verbose` and will output
detailed information about the parsing process for each file.

## Run modes

The tool is primarily designed for easy usage with small projects that have a Main file.
In case the project cannot be compiled with `javac Main.java`, or uses build tools (like Maven, Gradle, or Ant),
we cannot use the default compilation logic.

For this case, two additional run modes are available:

### instrument-only

By specifying the `-i <file|dir>` or `--instrument-only <file|dir>` mode the target file (or directory
with all its Java files) will be instrumented and written to the `.profiler/instrumented/` directory.
The `metadata.dat` file will also be generated.
The instrumented code can then be compiled by *custom* commands and run manually.

### generate-report-only

If a project was already instrumented and run, the HTML report can be quickly (re-)generated
with the `-r` or `--generate-report` run mode.
In this mode, no parsing or instrumentation will be done.
For it to succeed the `metadata.dat` and `counts.dat` files must already exist in the `.profiler` output directory.
