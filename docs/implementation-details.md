# Implementation details

This section gives some more information on how the profiler works in detail.

## Grammar/Parser
The Grammar consists of a reduced set of non-terminal symbols (NTS) that covers the most important aspects
of the Java 21 syntax, tailored for the profiler's use-case: finding the start and end position of blocks.

Using Coco's `ANY` keyword, we over-read non-relevant tokens like:

- access modifiers (`public`, `private`, ... )
- interfaces that a class `implements` (or superclasses)
- class level constants and member variables
- generic type definitions with angle brackets `<Type1<Type2,...>, ...>`
- array initializer blocks (starting with `{`), but we do not insert counters here
- a method’s argument list (within the parentheses)
- remaining tokens in a `GenericStatement` up to the semicolon
- a switch's case label(s), constant(s) and guard clause(s) before the colon or arrow

To build an index of classes and their methods, we have to keep track of class and method names 
in each file and assign them to the block objects in the model.
Package declarations at the beginning of a file have to be propagated to each class to determine fully qualified names.

Therefore, we used the semantic action syntax of Coco/R to insert our custom statements into the final generated parser file.
Arbitrary Java statements can be included in the ATG in the form of `(. STATEMENT; .)` blocks.
We use this to add hooks for our `ParserState` helper class.

E.g. to parse the full package name an NTS like this could be defined:

``` { title="JavaFile.atg" }
PackageDecl = "package"     (. ArrayList<String> packageName = new ArrayList<>(); .)
    ident                   (. packageName.add(t.val); .)
    {'.' ident              (. packageName.add(t.val); .)
    }
    ";"                     (. state.setPackageName(packageName); .)
.
```

Which would result in the following generated parser (pseudo-code) method: 
```java { title="Parser.java" hl_lines="3 5 9 12" }
void PackageDecl() {
    Expect("package");
    ArrayList<String> packageName = new ArrayList<>(); 
    Expect(IDENT);
    packageName.add(t.val); 
    while (la.kind == DOT) {
        Get();
        Expect(IDENT);
        packageName.add(t.val); 
    }
    Expect(";");
    state.setPackageName(packageName); 
}
```

In this way our state class is automatically updated during the recursive-descent parsing of Java source files,
and we can build our metadata on-the-go.

## Instrumentation
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
```java {hl_lines="1 3 4 9 11" }
"import auxiliary.__Counter;"
class Fibonacci {
  static int fib(int n) {"__Counter.inc(0);"
    if (n <= 1) {"__Counter.inc(1);"
      return n;
    }
    return fib(n - 1) + fib(n - 2);
  }
  public static void main(String[] args) {"__Counter.inc(2);"
    int N = Integer.parseInt(args[0]);
    for (int i = 1; i < N; i++) {"__Counter.inc(3);"
      System.out.print(fib(i) + " ");
    }
  }
}
```

Counter statements are always appended to the end of a line, to preserve the original line numbers.
This is especially important for getting correct lines numbers when an exception is thrown.

## The `__Counter` class

To successfully compile a copy of the program with additional `__Counter.inc(x)` statements,
we need to import the `__Counter` class inside each instrumented file.
The class is contained in a root-level `auxiliary` package so that can be imported at any level in the hierarchy.

A compiled `.class` version of `__Counter` is extracted from the tool JAR and copied
to the `instrumented/` and `classes/` output directories.

The counter class stores an array the size of all (number of) all found blocks in the entire project.
Every `__Counter.inc(idx)` statement includes the block index `idx` to increment.

By default, calls to `inc` are not synchronized to speed up runtime performance.
Using the `-s` / `--synchronized` option we insert `incSync` statements instead.
The counters are then kept in an `AtomicLongArray` to ensure exact results for multi-threaded programs.

## Special handling of language features

Some language syntax constructs required special non-trivial handling during instrumentation.

### Single-statements

We cannot just add a counter-statement to brace-less single-statement blocks.
<br/>
They have to be wrapped in braces for it to compile successfully.

The following example:

```java
boolean containsZero(int[][] array) {
  if (array == null) return false;
  for (int i = 0; i < array.length; i++)
    for (int j = 0; j < array[i].length; j++)
      if (array[i][j] == 0) return true;
  return false;
}
```

would require insertions of:

```java { hl_lines="1-5" }
boolean containsZero(int[][] array) {"__Counter.inc(261);"
  if (array == null)"{__Counter.inc(262);" return false;"}"
  for (int i = 0; i < array.length; i++)"{__Counter.inc(263);"
    for (int j = 0; j < array[i].length; j++)"{__Counter.inc(264);"
      if (array[i][j] == 0)"{__Counter.inc(265);" return true;"}}}"
  return false;
}
```

### Overloaded constructors

Java supports multiple “overloaded” constructors in the same class. If we use `super()`
or `this()` invocations, the language (currently) enforces that it must be the first statement in the
method body.
This makes it impossible to insert a `__Counter.inc(X);` statement right after the method's opening brace.

To handle this special case, we need to keep track of the **end** position of possible first `this/super` calls
in constructors.
The counter-statement is added only **after** this call.

```java { hl_lines="3 8" }
class SmallDog extends Dog {
  public SmallDog(String name, int age) {
    super(name, age);"__Counter.inc(3);"
    size = Size.SMALL;
    super.speak();
  }
  public SmallDog(String name, int age, Size s) {
    this(n, age);"__Counter.inc(4);"
    this.size = s;
  }
  ...
}
```

### Anonymous and local classes
As these full-fledged classes can appear anywhere inside a code block,
we need need to restore the previous state after entering, parsing, and exiting these inner classes.
For this, the `ParserState` class contains a Stack for methods, onto which we push
the current one when encountering class declarations inside methods. As soon as we exit the class,
we pop the method from the stack and continue parsing the outer method.

### Brace-less Lambdas

Lambda statements are often used without a method body, especially for stream processing.

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

The instrumented version would look like this:

```java { hl_lines="2-3" }
integers.stream()
        .peek(x ->"__Counter.incLambda(61, () ->" System.out.println(x)")")
        .filter(x ->"__Counter.incLambda(62, () ->"  x % 2 == 0")")
        .sum();
```

The compiler will automatically choose the fitting `incLambda` variant to call, depending on the return type.

### Switch statements and switch expressions

Switch statements need a lot of special handling due to their abnormal syntax.
<br/>
The switch block itself is not executable, `case` blocks are not enclosed in braces.
<br/>
Switch **expression** (introduced in Java 14) have the `yield` statement to return a value.
<br/>
We also can use arrow-cases (same `->` operator as for lambdas) to omit the `break`, in which case
there's either a curly-brace block or a single statement.

For the case of *single-statement* arrow-case expressions we need to wrap
the block in braces and add a `yield` keyword after the counter statement.
<br/>
In case a branch throws an exception, `yield` **must not** be added.

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

is instrumented as following:

```java { hl_lines="3-7" }
StatusCode statusCode = ...;
int sc = switch (statusCode) {
  case OK ->"{__Counter.inc(5); yield" 200;"}"
  case UNAUTHORIZED ->"{__Counter.inc(6); yield" 401;"}"
  case FORBIDDEN ->"{__Counter.inc(7); yield" 403;"}"
  case NOTFOUND -> {"__Counter.inc(8);" yield 404; }
  default -> "{__Counter.inc(9);" throw new RuntimeException("invalid code");"}"
};
```

### Control flow breaks
The keywords `break`, `continue`, `return`, `yield` and `throw` are used to exit a block early.
As our counters are inserted only at the **beginning** of blocks, we would need another counter
after every block (containing a control flow break statement) to correctly show line-hit coverage.

We took a different approach by introducing "code regions" to group together statements with the same hit count.
A code block is split into two regions when encountering an inner block.
If the inner block contains a control flow break, we subtract the hit count of the inner blocks
from the previous region's hit-count to calculate how frequently the following region was executed.

```java { hl_lines="4" }
275     | static int fib(int n) {
275     |   if (n <= 1)
275 142 |     return n;
275     |   return fib(n - 1) + fib(n - 2); // 275-142 = 133
        | }
```
