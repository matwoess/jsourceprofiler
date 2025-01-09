package org.matwoess.jsourceprofiler.fxui.util;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.common.OS;
import org.matwoess.jsourceprofiler.common.Util;
import org.matwoess.jsourceprofiler.fxui.model.AppState;
import javafx.beans.property.ObjectProperty;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * This class contains utility methods for interacting with the operating system.
 */
public class SystemUtils {
  /**
   * Opens a file chooser dialog and sets the chosen path on the given directory path property.
   *
   * @param dirProperty the directory path property to set
   */
  public static void chooseDirectory(ObjectProperty<Path> dirProperty) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setTitle("Choose Directory");
    Path initialDir = dirProperty.get();
    if (initialDir == null || !initialDir.toFile().isDirectory()) {
      initialDir = IO.getUserHomeDir();
    }
    dirChooser.setInitialDirectory(initialDir.toFile());
    File dirPath = dirChooser.showDialog(new Stage());
    if (dirPath != null) {
      dirProperty.set(dirPath.toPath());
    }
  }

  /**
   * Opens the specified path with its default desktop application.
   *
   * @param path the path to open
   */
  public static void openWithDesktopApplication(Path path) {
    SwingUtilities.invokeLater(() -> {
      try {
        Desktop.getDesktop().open(path.toFile());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Calls the tool with the given parameters in the system terminal.
   * @param appState the app state containing the parameters
   * @return the exit code of execution
   */
  public static int executeToolWithParameters(AppState appState) {
    String[] terminalCommand = getTerminalCommand(appState);
    return Util.runCommandInDir(appState.projectRoot.get(), terminalCommand);
  }

  /**
   * Builds an array of strings representing the command to be executed in the system terminal.
   * @param appState the app state containing the parameters
   * @return the command that can be executed in a system terminal using {@link ProcessBuilder}
   */
  public static String[] getTerminalCommand(AppState appState) {
    String javaCommand = getJavaRunCommand(appState.getProgramArguments());
    return appState.terminal.get().wrapWithTerminalCommand(javaCommand, appState.projectRoot.get());
  }

  /**
   * Builds the java command string to execute the tool with the given arguments.
   * @param toolArguments the arguments to be passed to the tool
   * @return the string representation of the java command
   */
  public static String getJavaRunCommand(String[] toolArguments) {
    var toolMainClass = org.matwoess.jsourceprofiler.tool.cli.Main.class;
    var classInCommon = org.matwoess.jsourceprofiler.common.Util.class;
    String toolJar = toolMainClass.getProtectionDomain().getCodeSource().getLocation().getPath();
    String commonJar = classInCommon.getProtectionDomain().getCodeSource().getLocation().getPath();
    String classPath = toolJar;
    if (!commonJar.equals(toolJar)) {
      classPath += OS.getOS().pathSeparator() + commonJar;
    }
    String[] toolMainCmd = {"java", "-cp", '"' + classPath + '"', toolMainClass.getName()};
    String[] fullCmd = Util.prependToArray(toolArguments, toolMainCmd);
    return String.join(" ", fullCmd);
  }
}
