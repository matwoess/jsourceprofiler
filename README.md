# jsourceprofiler

A source-code-instrumentation profiler for Java programs.
Designed to be a single-JAR command line tool that generates HTML reports. 
Optionally, a JavaFX GUI is available to configure the parameters and run the tool automatically.

The tool can parse Java **21** language syntax and should be executed with a JDK of version 17 or higher.

## Thesis Paper

This project was created as part of my master's thesis at the Institute for System Software ([SSW](https://ssw.jku.at/))
of the Johannes Kepler University ([JKU](https://www.jku.at/)), located in Linz, Austria.
Please see the [thesis paper](https://ssw.jku.at/Teaching/MasterTheses/JavaProfiler/Thesis.pdf) 
(published February 2024) or the documentation for further details.

## Documentation

The [Documentation](https://matwoess.github.io/jsourceprofiler)
contains a more comprehensive guide on how to install and use the tool.
It also explains implementation details and (current) limitations.

### Quick Links:
- [Introduction](https://matwoess.github.io/jsourceprofiler/)
- [Getting Started](https://matwoess.github.io/jsourceprofiler/getting-started)
- [Usage](https://matwoess.github.io/jsourceprofiler/usage)
- [JavaFx UI](https://matwoess.github.io/jsourceprofiler/fxui)
- [Report](https://matwoess.github.io/jsourceprofiler/report)

## Future Work

- VSCode plugin (TypeScript)
- IntelliJ IDEA plugin (Java)
- Gradle task plugin (Java)
- Be compatible with build tools like Ant / Gradle / Maven
- JavaFX GUI improvements: theme / usability
- ...
