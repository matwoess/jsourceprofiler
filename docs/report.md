# Report

The profiler generates an HTML report inside the `.profiler/report/` output directory. 
The `index.html` file can be opened in a browser to view it.

Additionally, a symbolic link or Windows shortcut will be created in the current working directory linking to the index file.

## Classes overview (index.html)

The main index file lists all classes found during parsing.

![Report classes overview](screenshots/report-class-overview.png)

By default, the list is sorted by the aggregated invocation count of all methods in each listed class.
This enables us to quickly identify hotspots in the program for its last run.
At the bottom of the list we find rarely or never used classes.

Two additional metrics are available:

- the method coverage in percent
- the hit-count of the "hottest" block inside the entire class

The columns are clickable and allow re-sorting of the rows by an alternative metric.
<br/>
Clicking on the class name will open the "Methods" index for this class.
<br/>
By clicking on the source file link, we can jump directly to this file's detail view.

## Methods overview (index_ClassName.html)

A separate method index is created for each *top-level* class.
<br/>
It lists all (non-abstract) methods, sorted by invocation count.

![Report methods overview](screenshots/report-method-overview.png)

The heading displays the fully-qualified name of the class (including package prefix).

Methods of inner classes are shown with the Java class file syntax: `Outer$Inner::Method`.
Anonymous and local classes get a numbered name `Outer$3::Method`, just like the compiled Java class files.

Clicking on a method name will jump into the source code view, directly to the line number of the method's declaration.

Browser-back or the button on top of the file can be used to return to the class overview page.

## Source file detail view (JavaFileName.html)

For each Java file, an annotated source code file is generated inside `.profiler/report/source/`.
It can be used to explore the methods and statements of each class in detail and get coverage information.

A small [jQuery](https://jquery.com/) script [file](https://github.com/matwoess/jsourceprofiler/tree/main/jsourceprofiler-tool/src/main/resources/js/highlighter.js) initializes
colors for relevant sections and dynamically updates them on mouse hover.
Syntax highlighting is provided by [highlight.js](https://highlightjs.org/). 
The line numbers are added by pure CSS and the hit-counter column is created dynamically on page-load.

![Report source file](screenshots/report-source-view.png)

Code blocks that have been entered at least once during program execution
are highlighted in <span style="color: lightgreen">green</span>.
Never-covered blocks are displayed with a <span style="color: indianred">red</span> background color.

Statements with the same hit count are grouped into "code regions".
These regions are shown in a darker, more opaque green and red (depending on their coverage status).

The column, to the right of the line numbers, contains the region's hit counts for each line.
<br/>
If **multiple** code regions start in the **same** line, the hits are shown stacked next to each other.

Hovering over a block or region will highlight the entire block in
<span style="color: gold">yellow</span> and statements of the current region in
<span style="color: orange">orange</span>.
The current region's code will also become **bold** until moving the mouse away from it.
Additionally, a popup will show the number of hits from the current block or region.
