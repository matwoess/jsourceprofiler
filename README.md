# jsourceprofiler

A source-code-instrumentation profiler for Java programs.
Designed to be a single-JAR command line tool that generates HTML reports. 
Optionally, a JavaFX GUI is available to configure the parameters and run the tool automatically.

The tool can parse Java **21** language syntax and should be executed with a JDK of version 17 or higher.

## Quick links:
- [Introduction](#introduction)
- [Download](#download)
- [Building from source](#building-from-source)
- [Usage](#usage)
- [FxUI](#fxui)
- [Report](#report)
- [Implementation details](#implementation-details)
- [Dependencies](#dependencies)
- [Future work and ideas](#future-work-and-ideas)

## Download
The [Releases](https://github.com/matwoess/jsourceprofiler/releases/) section contains downloadable `.jar`-archives for the tool and the FxUI tool-runner.

The UI has separate releases for Windows, Linux and macOS.
<br/>
The "fxui" zip archives provide executable scripts to directly start the GUI without using the command line.

## Building from source
To build the project from source, Gradle and a Java JDK of version 21 or newer are required.

After cloning or downloading the repository, the `Scanner.java` and `Parser.java` files must first 
be generated using the [Coco/R library](https://ssw.jku.at/Research/Projects/Coco/Java/Coco.jar).

This can be done by running the provided `generate-parser.sh`  bash script, 
or the `generate-parser.ps1` PowerShell script in the [scripts/](scripts) folder. 

Both should automatically download the library and execute it on the project's [ATG file](jsourceprofiler-tool/src/main/java/org/matwoess/jsourceprofiler/tool/instrument/JavaFile.atg).

This can also be done manually by downloading Coco/R and executing the following command in the 
project root directory, to re-generate the parser files at any time:

```shell
java -jar lib/Coco.jar \
  -package org.matwoess.jsourceprofiler.tool.instrument \
  jsourceprofiler-tool/src/main/java/org/matwoess/jsourceprofiler/tool/instrument/JavaFile.atg 
```

## Usage
```
Usage: profiler [options] <main file> [program args]  
Or   : profiler [options] <run mode>
```
The Java binaries `java` and `javac` should be included in the system environment path variable for it to work.

All output is stored in the hidden `.profiler` subdirectory of the current working directory.

For being able to use local project resources and relative paths (as arguments or inside the program)
the tool never changes its directory during execution and makes use of classpath arguments instead.
It is therefore recommended to run the command line tool in the project's root directory.

If the Main file references other source files the `-d`/`--sources-directory` parameter is **required**!

### Sample usage and explanation
(In the below section the `java -jar profiler.jar` command is substituted by `profiler`)

In the simplest case, the tool can be used as following:
```shell
profiler Main.java arg1 arg2 ...
```
This will parse the given file and create an instrumented copy in the `.profiler/instrumented/` folder. 
The first argument to the tool specifies the class containing the main entry point.
<br/>
Additionally the `.profiler/metadata.dat` file will be created, containing information about 
every found code block like its begin/end position, its parent method/class and other relevant data to create a report.

The tool will automatically compile the instrumented version using `javac`.
```shell
javac -cp .profiler/instrumented -d .profiler/classes Main.java
```
The Java compiler itself finds referenced Java files (used in Main) and will compile them also 
into (instrumented) `.class` files.<br/>
The compiled classes can be found in the `.profiler/classes/` directory.

Next, the `java` binary will be used to execute the specified class by name (without the `.java` extension) 
with the given arguments:
```
java -cp .profiler/classes Main arg1 arg2 ...
```
Executing the instrumented files, automatically stores the hit-counter values in `.profiler/counts.dat` 
as soon as the program ends.

Finally, the metadata and counts will be used to create the report inside `.profiler/report/`.

### Command line options
There are a few optional arguments available. For a full list, run `profiler -h` or `profiler --help`.

#### sources-directory
If the project-to-profile consists of two or more (linked) Java files, the sources directory has to be specified.
This is done with the `-d` or `--sources-directory` option:
```shell
profiler -d src/main/java/ src/main/java/subfolder/Main.java
```

Using this option, all `.java` files inside `src/main/java/` will be parsed, instrumented and copied to the 
"instrumented" directory. The relative folder-structure is replicated to ensure successful compilation (`javac` will
throw an error if the package name and file paths mismatch).

#### synchronized
When adding `-s` or `--synchronized` as a option, all inserted counters will be incremented atomically. 
This might be useful for multi-threaded programs, where a few methods or blocks are constantly executed in parallel.
It will ensure that hit counts are correct, but runtime performance will be impacted.

#### verbose
This option is mainly for debugging purposes. It can be activated with `-v` or `--verbose` and will output 
detailed information about the parsing process for each file.

### Run modes

The tool is primarily designed for easy usage with small projects that have a Main file. 
In case the project cannot be compiled with `javac Main.java`, or uses build tools (like Maven, Gradle, or Ant), 
we cannot use the default compilation logic.

For this case, two additional run modes are available:

#### instrument-only

By specifying the `-i <file|dir>` or `--instrument-only <file|dir>` mode the target file (or directory 
with all its Java files) will be instrumented and written to the `.profiler/instrumented/` directory. 
Also, the `metadata.dat` file is generated.
The instrumented code can then be compiled by custom commands and run manually.

#### generate-report-only

If a project was already instrumented and run, the HTML report can be quickly (re-)generated 
with the `-r` or `--generate-report` run mode.
In this mode, no parsing or instrumentation will be done.
For it to succeed the `metadata.dat` and `counts.dat` files must already exist in the output directory.

## FxUI

A graphical application (using th [JavaFX](https://openjfx.io/) toolkit) was created, to easily configure parameters and arguments 
for the command-line tool. The profiler is then executed in a terminal.

Golden `(?)` labels can be hovered over for more information about each field.

### Open project dialog

Before displaying the main window, a project directory must be chosen, using a selection dialog.

![FxUI project selection dialog](/screenshots/fxui-project-selection-dialog.png)

Clicking "Select" will show the system's native dialog to choose a folder.
<br/>
Alternatively, the path can be entered directly into the text field.

As soon as a valid folder path is entered, the main application window can be invoked with the "Open" button.

On the next program execution, the previously opened path will be pre-filled.

### Main application window

![FxUI main window](/screenshots/fxui-main-application-window.png)


Using the file tree on the left, the sources directory and main file can be selected. Depending on the run mode 
this may be required.
<br/>
Assigning a file or directory to a parameter can be done with the <kbd>Return</kbd> key, or using the context menu
on a tree item.

The tree highlights important items in color:
- <span style="color: blue;">blue</span> - selected sources directory,
- <span style="color: green;">green</span> - selected main file.
- <span style="color: brown;">brown</span> - output directory

The menu bar allows rebuilding the file tree, and saving or restoring currently set parameters (will be saved in the 
output directory as `parameters.dat`).

When clicking "Run tool", a system-native terminal (can be chosen) will be opened, to show program output and 
to allow user input (for interactive programs).

The executed command can be previewed with the "Preview command" button.

"Open report" will only show up once the `.profiler/report/index.html` file exists.
Clicking it calls the system's default application for HTML files (browser).

### Theme

The UI uses the `PrimerDark` theme from [AtlantaFX](https://github.com/mkpaz/atlantafx) as a `userAgentStylesheet`.

## Report

In the default run mode (or by using the `-r` mode) an HTML report will be generated inside the output directory.
<br/>
It is stored in the `.profiler/report/` folder. The `index.html` file can be opened in a browser to view it.

### Classes (index.html)

The main index lists all classes found during parsing.
<br/>

![Report classes overview](/screenshots/report-class-overview.png)

By default, the list is sorted by the aggregated invocation count of all methods in each listed class.
This enables us to quickly identify hotspots in the program for its last run.<br/>
At the bottom of the list we find rarely or never used classes.

Two additional metrics are available:
- the method coverage in percent and 
- the hit-count of the "hottest" block inside the whole class.

The columns are clickable and allow re-sorting of the rows by an alternative metric.

Clicking on the class name will open the "Methods" index for this class.
<br/>
By clicking on the source file link, we can jump directly to the source file detail report.

### Methods (index_ClassName.html)

A separate method index is created for each *top-level* class.
<br/>
It lists each non-abstract method, sorted by invocation count.

![Report methods overview](/screenshots/report-method-overview.png)

The heading displays the fully qualified name of the class.

Methods of inner classes are shown with the java class-file syntax: `Outer$Inner::Method`.
<br/>
Anonymous and local classes get a numbered name `Outer$3::Method`, just like in the compiled class files.

Clicking on a method name will jump into the source detail report, to the line number of the method's declaration.

Browser-back or the top button can be used to return to the class overview.

### Source file detail report (JavaFileName.html)

For each Java file, an annotated source code file is generated inside `.profiler/report/source/`.
It can be used to explore each class and its method in detail.

A small [jQuery](https://jquery.com/) script [file](jsourceprofiler-tool/src/main/resources/highlighter.js) initializes 
colors for relevant sections and dynamically updates them on mouse hover.

![Report source file](/screenshots/report-source-view.png)

Code blocks that have been entered at least once during program execution 
are highlighted in <span style="color: green">green</span>. 
<br/>
Non-covered blocks are displayed with <span style="color: red">red</span> background color.

Statements with the same hit count are grouped into "code regions".
<br/>
These regions are shown in a darker, more opaque green or red (depending on their coverage status).

The column, to the left of line numbers, contains the region hit counts for each line.
<br/>
If multiple code regions are located in the same line, the hits are shown stacked next to each other.

Hovering over a block or region will highlight the entire block in
<span style="color: yellow">yellow</span> and the current region in 
<span style="color: orange">orange</span>.<br/>
The current region's code will also become bold until moving the mouse away from the region.
<br/>
Additionally, a popup-hint will show the number of hits.

## Implementation details

This section gives some more information on how the profiler works in detail.

### Grammar/Parser
The Grammar consists of a reduced set of non-terminal symbols (NTS) that covers the most important aspects 
of the Java 21 syntax tailored for the profiler's use-case (finding the begin and end position of blocks).

Using Coco's `ANY` keyword, we over-read non-relevant tokens like:
- access modifiers (`public`, `private`, ... )
- interfaces that a class `implements` (or superclasses)
- class level constants and member variables
- generic type definitions with angle brackets `<Type1<Type2,...>, ...>`
- array initializer blocks (starting with `{`), but we do not insert counters here
- a method’s argument list (within the parentheses)
- remaining tokens in a `GenericStatement` up to the semicolon
- the switch-case label(s), constant(s) and guard clause(s) before the colon or arrow

To build an index of classes and their methods, we have to keep track of class names 
and method names in each file and assign them to the blocks in the model.
<br/>
Also, package declarations at the beginning of a file will be inherited by each class 
in this file for knowing its fully qualified name.

### Instrumentation
The tool parses source code files and stores an instrumented copy in the output directory.
<br/>
At the beginning of executable code blocks (usually after an opening brace `{`). a `__Counter.inc(X);` statement is
inserted.

A program like:
```java
class Fibonacci {
  static int fib(int n) {
    if (n <= 1) {
      return n;
    }
    return fib(n - 1) + fib(n - 2);
  }
  public static void main(String[]args) {
    int N = Integer.parseInt(args[0]);
    for (int i = 1; i < N; i++) {
      System.out.print(fib(i) + " ");
    }
  }
}
```

Would look in its instrumented version something like this:
```java
'import auxiliary.__Counter;'
class Fibonacci {
  static int fib(int n) {'__Counter.inc(0);'
    if (n <= 1) {'__Counter.inc(1);'
      return n;
    }
    return fib(n - 1) + fib(n - 2);
  }
  public static void main(String[] args) {'__Counter.inc(2);'
    int N = Integer.parseInt(args[0]);
    for (int i = 1; i < N; i++) {'__Counter.inc(3);'
      System.out.print(fib(i) + " ");
    }
  }
}
```

### The `__Counter` class

To successfully compile a copy of the program with additional `__Counter.inc(x)` statements,
we need to import the `__Counter` class inside each instrumented file.
The class is contained in a root-level `auxiliary` package and can be imported at any hierarchy level.

A compiled `.class` version of `__Counter` is extracted from the tool JAR and copied
to the `instrumented/` and `classes/` output directories.

The class stores an array the size of all (number of) found blocks in the entire project.
Every `__Counter.inc(idx)` statement includes the block-index to increment.

By default, calls to `inc` are not synchronized to speed up runtime performance.
Using the `-s` option we insert `incSync` statements instead.
The counters are then kept in an `AtomicLongArray` to ensure exact results for multi-threaded programs.

### Special handling of language features

Some language syntax required non-trivial special handling.

#### Single-statements

We cannot just add a counter-statement to single-statement blocks. 
<br/>
In this case, we have to wrap the block in braces for it to compile.

```java
boolean containsZero(int[][] array) {
  if (array == null) return false;
  for (int i = 0; i < array.length; i++)
    for (int j = 0; j < array[i].length; j++)
      if (array[i][j] == 0) return true;
  return false;
}
```

would require insertion of:

```java
boolean containsZero(int[][] array) {'__Counter.inc(261);'
  if (array == null)'{__Counter.inc(262);' return false;'}'
  for (int i = 0; i < array.length; i++)'{__Counter.inc(263);'
    for (int j = 0; j < array[i].length; j++)'{__Counter.inc(264);'
      if (array[i][j] == 0)'{__Counter.inc(265);' return true;'}}}'
  return false;
}
```

#### Overloaded constructors

Java supports multiple “overloaded” constructors in the same class. If we use `super()`
or `this()` invocations, the language enforces that it must be the first statement in the
method body. 
This makes it impossible to insert a `__Counter.inc(X);` statement right after the method's opening brace.

To handle this special case, we need to keep track of the **end** position of possible first `this/super` calls
in constructors.
The counter-statement is added only after this call.

```java
class SmallDog extends Dog {
  public SmallDog(String name, int age) {
    super(name, age);'__Counter.inc(3);'
    size = Size.SMALL;
    super.speak();
  }
  public SmallDog(String name, int age, Size s) {
    this(n, age);'__Counter.inc(4);'
    this.size = s;
  }
  ...
}
```

#### Anonymous and local classes
As these full-fledged classes can appear anywhere inside a code block,
we need need to restore the previous state after parsing and exiting these inner classes.
For this, the `ParserState` class contains a Stack of methods, onto which we push
the current one when encountering class declarations inside methods.

#### Brace-less Lambdas

Especially for stream processing, lambda statements are often used without a method body.

Given the example:

```java
integers.stream()
        .peek(x -> System.out.println(x))
        .filter(x -> x % 2 == 0)
        .sum();
```

We need a clever way to observe how often each lambda statement was executed.

The `__Counter` class contains a special `incLambda` method that wraps these lambdas
as an argument into either a generic anonymous `Runnable` or `Supplier<T>`.

The instrumented version will look like this:

```java
integers.stream()
        .peek(x ->'__Counter.incLambda(61, () ->' System.out.println(x)')')
        .filter(x ->'__Counter.incLambda(62, () ->'  x % 2 == 0')')
        .sum();
```

The compiler will automatically choose the fitting `incLambda` variant to call, depending on the return type.

#### Switch statements and switch expressions

Switch statements need a lot of special handling due to their abnormal syntax.
<br/>
The switch block itself is not executable, case blocks are not enclosed in braces.
<br/>
Switch *expression* (since Java 14) have the `yield` statement to return a value.
<br/>
We also can use arrow-cases (`->` like lambdas) to omit the `break`, in which case 
there's either a curly-brace block or a single statement.

For the case of *single-statement* arrow-case expressions we need to wrap 
the block in braces and add a `yield` keyword after. 
<br/>
In case a branch throws an exception, `yield` must **not** be added.

The following example:

```java
StatusCode statusCode = ...;
int sc = switch (statusCode) {
  case OK -> 200;
  case UNAUTHORIZED -> 401;
  case FORBIDDEN -> 403;
  case NOTFOUND -> { yield 404; }
  default -> throw new RuntimeException("invalid code");
};
```

is as following instrumented like this:

```java
StatusCode statusCode = ...;
int sc = switch (statusCode) {
  case OK ->'{__Counter.inc(5); yield' 200;'}'
  case UNAUTHORIZED ->'{__Counter.inc(6); yield' 401;'}'
  case FORBIDDEN ->'{__Counter.inc(7); yield' 403;'}'
  case NOTFOUND -> {'__Counter.inc(8);' yield 404; }
  default -> '{__Counter.inc(9);' throw new RuntimeException("invalid code");'}'
};
```

#### Control flow breaks
The keywords `break`, `continue`, `return`, `yield` and `throw` are used to exit a block early.
As our counters are inserted only at the **beginning** of blocks, we would need another counter 
after every block containing a control flow break statement to correctly show line-hit coverage.

We took a different approach by introducing "code regions" to group statements with the same hit count.
A code block is split into two regions when encountering an inner block.
If the inner block contains a control flow break, we subtract the hit count of the inner blocks 
from the first region's hits to calculate how frequently the second region was executed.

```java
275     | static int fib(int n) {
275     |   if (n <= 1)
275 142 |     return n;
275     |   return fib(n - 1) + fib(n - 2); // 275-142 = 133
        | }
```

## Runtime impact
To evaluate how much the inserted counter statements impact the run-time performance of a program,
we ran a few benchmarks of the [DaCapo Benchmark Suite](https://dacapobench.sourceforge.net/) 
in three different configurations:

- "orig" — The original unmodified benchmark project without instrumentation
- "instr" — A version with counter-increment statements added to every code block
- "sync" — The benchmark with synchronized counters, using the `AtomicLongArray`

The following figure shows the average relative run-time overhead of seven benchmarks 
programs compared to their un-instrumented version:

![Relative runtime overhead in the DaCapo benchmarks](/screenshots/runtime-impact.png)

Most benchmarks show only a relatively small slowdown to less than 200% run time.
The h2 program does not show any significant impact as most of its work in performed 
in its derby database library, which is not instrumented.
The sunflow benchmark (multi-threaded CPU ray-tracing) is the opposite extreme, 
showing a significant 10-fold run time impact when using synchronized counters.

For further analysis and details, see the thesis paper.

## Limitations

### General
- Only the project itself is instrumented (no library classes without source code)
- Run-time exceptions inside and outside of try blocks cannot be considered for the
  resulting coverage data (of the following statements).

### Temporary
- Custom build tools (like Ant, Maven and Gradle) are not supported yet.
- Imperfect grammar:
  - The ATG is kept simple, minimal and generic. While we can successfully parse and instrument large projects, 
    we do not claim to find every possible code block. The fuzzy approach leads to some special structures
    being currently skipped.
- The hit count alone does tell us how long the method execution took


## Dependencies

- [OpenJDK 21](https://openjdk.org/projects/jdk/21/)
- [Coco/R](https://ssw.jku.at/Research/Projects/Coco/#Java)
- [Gradle](https://gradle.org/)
- [jQuery](https://jquery.com/)
- [JavaFX](https://openjfx.io/)
- [AtlantaFX](https://github.com/mkpaz/atlantafx)
- [Highlight.js](https://highlightjs.org/)
- [Gradle Maven Publish Plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/)

## Future work and ideas

- VSCode plugin (TypeScript)
- IntelliJ IDEA plugin (Java)
- Gradle task plugin (Java)
- Be compatible with build tools like Ant / Gradle / Maven
- JavaFX GUI improvements: theme / usability
- ...
