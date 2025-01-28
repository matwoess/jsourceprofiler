# Tool Workflow

This section further describes the inner workings of **jsourceprofiler** in more detail.

The workflow of the profiler is divided into the five stages:

![The five main steps of the profiler](screenshots/profiler-steps.png)

## Analyze and Instrument
After parsing all Java sources, the `.profiler/metadata.dat` file will be created.
It contains information about every found code block, used for instrumentation and creation of the report. 

Some example data are:

- the unique ID of a block (index in order of found)
- the start/end position and line number
- a pointer to the outer block (if any)
- the block's parent method/class/package
- its type (method, if, for, switch, ...)

All instrumented source file copies are written to `.profiler/instrumented/`.

## Compile

The tool will then automatically compile the instrumented version using the `javac` compiler.
```shell title="(the tool executes this command internally)"
javac -cp .profiler/instrumented -d .profiler/classes App.java
```
(whereas `App.java` is the specified main file)

The Java compiler finds referenced Java files (used in `App`) automatically and compiles them next
into (instrumented) `.class` files. The compiled classes will be written to the `.profiler/classes/` directory.

Inserted counters use the `auxiliary.__Counter` class to increment the hit-counters. For the instrumented code to compile,
the pre-built `auxiliary` package is extracted and copied to `.profiler/instrumented/` by the profiler.

Classes that are **not** used in the main file or its referenced classes will **not** be compiled.

## Execute

Next, the `java` binary is used to execute the instrumented copy of the specified class by name (without the `.java` extension)
and with the given arguments:
```shell title="(the tool executes this command internally)"
java -cp .profiler/classes App arg1 arg2 ...
```

Again, for this to work, the `auxiliary` package is copied to the `.profiler/classes/` directory.

Executing the instrumented files, automatically stores the hit-counter values in `.profiler/counts.dat`
as soon as the program execution is finished and the JVM shuts down.
This is done by a shutdown hook in the `__Counter` class and a hard-coded path.

```java title="auxiliary.__Counter.java"
public class __Counter {
  static {
    init(".profiler/metadata.dat");
    Runtime.getRuntime().addShutdownHook(
        new Thread(() -> save((".profiler/counts.dat"))));
  }
  ...
}
```

## Report

Finally, the metadata and counts will be used to generate the report inside `.profiler/report/`.

Afterward, a `report.html` link to the report will be created in the current working directory, pointing to `.profiler/report/index.html`.
On Linux or macOS it will be a symbolic link, on Windows a `report.lnk` shortcut file.

The HTML report can then be used to find hotspots during the last execution (further described in the [Report](report.md) section).