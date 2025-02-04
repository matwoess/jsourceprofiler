# Getting Started

## Download and use
The [Releases](https://github.com/matwoess/jsourceprofiler/releases/) section contains downloadable `.jar`-archives for the tool and portable zip files containing the 
JavaFX tool-runner application. The command-line tool is platform-independent and can be run on any system with a 
Java Development Kit (JDK) installed.
The "fxui" has separate releases for Windows, Linux and macOS.
The archives provide executable scripts to directly start the GUI without using the command line 
(located in the `bin/` subfolder).

For detailed instructions on how to use the command-line tool, see the [Usage](usage.md) section.
For an introduction to the JavaFX tool-runner GUI, see the [JavaFX UI](fxui.md) section.

## Building from source
To build the project from source, Gradle and a Java JDK of version 21 or newer are required.

After that it's as simple as:
```shell
./gradlew build
```

### Generating the parser (automatic task)

After cloning or downloading the source code, the `Scanner.java` and `Parser.java` files must first
be generated using the [Coco/R library](https://ssw.jku.at/Research/Projects/Coco/Java/Coco.jar).
This should be done automatically by our custom `generateParser` Gradle build step.
It will download Coco/R to the `lib/` folder and use it to create the needed files using the project's
[ATG file](https://github.com/matwoess/jsourceprofiler/tree/main/jsourceprofiler-tool/src/main/parsergen/JavaFile.atg) 
(together with the `Scanner.frame` and `Parser.frame` files). The task is added as a dependency to the `compileJava` 
and `sourcesJar` tasks and will be called by Gradle when building the project.

This step could also be done manually by downloading Coco/R and executing the following command in the
project's root directory, to (re-)generate the parser files at any time:

```shell
java -jar jsourceprofiler-tool/lib/Coco.jar \
  -o jsourceprofiler-tool/src/main/java/org/matwoess/jsourceprofiler/tool/instrument \
  -package org.matwoess.jsourceprofiler.tool.instrument \
  jsourceprofiler-tool/src/main/parsergen/JavaFile.atg 
```

### Additional build targets

To create a single "fat" JAR that contains both `jsourceprofiler-tool` and `jsourceprofiler-common` in one archive
(for easier use with the command-line) a `fatJar` build target is provided.
This task is currently used to make the `profiler-x.y.z.jar` artifacts for the [Releases](https://github.com/matwoess/jsourceprofiler/releases/) section.

## Include as a dependency

Additionally to the ready-to-use releases, the tool can be included in other projects as a Maven dependency.
It is available on the [GitHub Package Registry](https://github.com/matwoess?tab=packages&repo_name=jsourceprofiler) 
and on [Maven Central](https://central.sonatype.com/search?q=jsourceprofiler&namespace=org.matwoess).

The command-line tool can be included in a Maven project by adding the following dependency to the `pom.xml` file:

```xml
<dependency>
  <groupId>org.matwoess</groupId>
  <artifactId>jsourceprofiler-tool</artifactId>
  <version>0.12.0</version>
</dependency>
<dependency>
  <groupId>org.matwoess</groupId>
  <artifactId>jsourceprofiler-common</artifactId>
  <version>0.12.0</version>
</dependency>
```

When using Gradle, the following lines can be added to the `build.gradle` file:

```kotlin
dependencies {
    implementation("org.matwoess:jsourceprofiler-tool:0.12.0")
    implementation("org.matwoess:jsourceprofiler-common:0.12.0")
}
```