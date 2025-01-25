# FxUI

An easy-to-use graphical application was created (using th [JavaFX](https://openjfx.io/) toolkit), to configure parameters and arguments
for the command-line tool. It automatically calls the tool in a new terminal window.

Golden <span style="color: darkgoldenrod;">(?)</span> labels can be hovered over for more information about each field.

## Open project dialog

Before displaying the main window, a project directory must be chosen, using a selection dialog.

![FxUI project selection dialog](screenshots/fxui-project-selection-dialog.png)

Clicking <kbd>Select</kbd> will show the system's native dialog to choose a folder.
<br/>
Alternatively, the path can be entered directly into the text field.

As soon as a valid folder path is entered, the main application window can be invoked with the <kbd>Open</kbd> button.

The opened path will be stored locally and pre-filled on the next program execution.

## Main application window

![FxUI main window](screenshots/fxui-main-application-window.png)


Using the file tree on the left, the sources directory and main file can be selected. Depending on the run mode
this may be required.
<br/>
Assigning a file or directory to a parameter can be done with the <kbd>Return</kbd> key, or using the context menu
on a tree item.

The tree highlights important items in color:

- <span style="color: blue;">blue</span> for the selected sources directory,
- <span style="color: green;">green</span> for selected main file.
- <span style="color: brown;">brown</span> for the tool's (hidden) output directory

The top menu bar allows rebuilding the file tree and saving or restoring currently set parameters (will be saved in the
output directory as `parameters.dat`).

When clicking "Run tool", a system-native terminal will be opened, to show program output and
to allow user input (useful for interactive programs).
The UI allows to customize the terminal application to use.

The executed command can be previewed with the <kbd>Preview command</kbd> button.

<kbd>Open report</kbd> will only show up once the `.profiler/report/index.html` file exists.
Clicking it calls the system's default application for HTML files (usually the browser).

## JavaFX Theme

The UI uses the `PrimerDark` theme from [AtlantaFX](https://github.com/mkpaz/atlantafx) as a `userAgentStylesheet`.
