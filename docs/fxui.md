# JavaFX UI

An easy-to-use graphical application was created (using the [JavaFX](https://openjfx.io/) toolkit), to configure parameters and arguments
for the command-line tool. It automatically calls the tool in a new terminal window.

Golden <span style="color: darkgoldenrod;">(?)</span> labels can be hovered over for more information about each field.

## Open project dialog

Before displaying the main application view, a project directory must be chosen, using the initial selection dialog.

![FxUI project selection dialog](screenshots/fxui-project-selection-dialog.png)

Clicking <kbd>Select</kbd> will open the system's native file-picker dialog to choose a folder.
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

- <span style="color: royalblue;">blue</span> for the selected sources directory,
- <span style="color: green;">green</span> for selected main file.
- <span style="color: brown;">brown</span> for the tool's (hidden) output directory

The top menu bar allows rebuilding the file tree and saving or restoring currently set parameters (will be saved in the
output directory as `parameters.dat`).

When clicking <kbd>Run tool</kbd>, a system-native terminal will be launched, showing the program output and
allowing user input (useful for interactive programs).
The terminal application to use can be customized in the user interface.

The effectively executed terminal command can be previewed with the <kbd>Preview command</kbd> button.

<kbd>Open report</kbd> will only show up once the `.profiler/report/index.html` file exists.
Clicking it opens the report in the system's default application for HTML files (usually a browser).

## JavaFX Theme

The UI currently uses the `PrimerDark` theme from [AtlantaFX](https://github.com/mkpaz/atlantafx) as a `userAgentStylesheet`.
