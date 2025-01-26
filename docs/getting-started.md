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

After cloning or downloading the source code, the `Scanner.java` and `Parser.java` files must first
be generated using the [Coco/R library](https://ssw.jku.at/Research/Projects/Coco/Java/Coco.jar).
This can be done by running the provided `generate-parser.sh`  bash script,
or the `generate-parser.ps1` PowerShell script in the
[scripts/](https://github.com/matwoess/jsourceprofiler/tree/main/scripts) folder.
Both will automatically download Coco/R to the `lib/` folder and use it to create the needed files using the project's
[ATG file](https://github.com/matwoess/jsourceprofiler/tree/main/jsourceprofiler-tool/src/main/java/org/matwoess/jsourceprofiler/tool/instrument/JavaFile.atg) (together with the `Scanner.frame` and `Parser.frame` files).

This step can also be done by downloading Coco/R manually and executing the following command in the
project's root directory, to (re-)generate the parser files at any time:

```shell
java -jar lib/Coco.jar \
  -package org.matwoess.jsourceprofiler.tool.instrument \
  jsourceprofiler-tool/src/main/java/org/matwoess/jsourceprofiler/tool/instrument/JavaFile.atg 
```

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