# Getting Started

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
or the `generate-parser.ps1` PowerShell script in the
[scripts/](https://github.com/matwoess/jsourceprofiler/tree/main/scripts) folder.

Both should automatically download the library and execute it on the project's
[ATG file](https://github.com/matwoess/jsourceprofiler/tree/main/jsourceprofiler-tool/src/main/java/org/matwoess/jsourceprofiler/tool/instrument/JavaFile.atg).

This can also be done manually by downloading Coco/R and executing the following command in the
project root directory, to re-generate the parser files at any time:

```shell
java -jar lib/Coco.jar \
  -package org.matwoess.jsourceprofiler.tool.instrument \
  jsourceprofiler-tool/src/main/java/org/matwoess/jsourceprofiler/tool/instrument/JavaFile.atg 
```
