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
