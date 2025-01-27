# Tool Workflow

This section further describes the inner workings of the **jsourceprofiler** tool in more detail.

The workflow of the profiler is divided into the following stages:

![The five main steps of the profiler](screenshots/profiler-steps.png)

## Analyze and Instrument
After parsing all Java sources, the `.profiler/metadata.dat` file will be created.

It contains information about every found code block like:

- its unique ID
- the begin/end position and line number
- a pointer to the outer block (if any)
- its parent method/class/package
- its type (method, if, for, while, ...) to know how to instrument it
- other relevant data for creating the report

All instrumented source file copies will be written to the `.profiler/instrumented/` directory.

## Compile

The tool will then automatically compile the instrumented version using the `javac` compiler.
```shell title="(the tool executes this command internally)"
javac -cp .profiler/instrumented -d .profiler/classes App.java
```
(whereas `App.java` is the specified main file)

The Java compiler finds referenced Java files (used in `App`) automatically and compiles them next
into (instrumented) `.class` files. The compiled classes will be written to the `.profiler/classes/` directory.

Inserted counter use the `auxiliary.__Counter` class to increment the hit-counters. For the instrumented code to compile,
the pre-compiled `auxiliary` package is extracted and copied to the `.profiler/instrumented/` by the tool.

Classes that are **not** used in the main file or its linked classes will **not** be compiled.

## Execute

Next, the `java` binary is used to execute the specified class by name (without the `.java` extension)
and with the given arguments:
```shell title="(the tool executes this command internally)"
java -cp .profiler/classes App arg1 arg2 ...
```

Again, for this to work the `auxiliary` package is copied to the `.profiler/classes/` directory.

Executing the instrumented files, automatically stores the hit-counter values in `.profiler/counts.dat`
as soon as the program execution is finished and the JVM shuts down.

## Report

Finally, the metadata and counts will be used to create the report inside `.profiler/report/`.

A link to the report will be created in the current working directory, pointing to `.profiler/report/index.html`.
On Linux or macOS it will be a symbolic link, on Windows a `report.lnk` shortcut link.