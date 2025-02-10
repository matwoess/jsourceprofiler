# jsourceprofiler

A source-code-instrumentation profiler for Java programs.
Designed to be a single-JAR command line tool that generates HTML reports. 
Optionally, a JavaFX GUI is available to configure the parameters and run the tool automatically.

The tool can parse Java **21** language syntax and should be executed with a JDK of version 17 or higher.

## Documentation

The [Documentation](https://matwoess.github.io/jsourceprofiler)
contains a comprehensive guide on how to install and use the tool.
It also explains its workflow, implementation details and (current) limitations.

### Quick Links:
- [Introduction](https://matwoess.github.io/jsourceprofiler/)
- [Getting Started](https://matwoess.github.io/jsourceprofiler/getting-started)
- [Usage](https://matwoess.github.io/jsourceprofiler/usage)
- [JavaFx UI](https://matwoess.github.io/jsourceprofiler/fxui)
- [Report](https://matwoess.github.io/jsourceprofiler/report)

For an overview over the source code the page also hosts the Javadoc API:

- [Javadoc API](https://matwoess.github.io/jsourceprofiler/docs/api)
- [Javadoc API (Dark Theme)](https://matwoess.github.io/jsourceprofiler/darkdocs/api)

## Thesis Paper

This project was created as part of my master's thesis at the Institute for System Software ([SSW](https://ssw.jku.at/))
of the Johannes Kepler University ([JKU](https://www.jku.at/)), located in Linz, Austria.
For a more in-depth (but slightly out of date) source of information, please see the [thesis paper](https://ssw.jku.at/Teaching/MasterTheses/JavaProfiler/Thesis.pdf)
(published February 2024).

## Future Work

- VSCode plugin (TypeScript)
- IntelliJ IDEA plugin (Java)
- Gradle task plugin (Java)
- Be compatible with build tools like Ant / Gradle / Maven
- JavaFX GUI improvements: theme / usability
- ...
