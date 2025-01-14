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
