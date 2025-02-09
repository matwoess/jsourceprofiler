package org.matwoess.jsourceprofiler.fxui.model;

import org.matwoess.jsourceprofiler.common.OS;

import java.nio.file.Path;

/**
 * A class enumerating all supported terminal applications.
 * Each terminal has its own way to wrap commands and how to call it.
 */
public enum Terminal {
  /**
   * The Windows Command Prompt.
   */
  WINDOWS_CMD {
    @Override
    String getExecutable() {
      return "cmd.exe";
    }
  },
  /**
   * The Windows PowerShell.
   */
  WINDOWS_POWERSHELL {
    @Override
    String getExecutable() {
      return "powershell.exe";
    }
  },
  /**
   * The GNOME Terminal.
   */
  GNOME_TERMINAL {
    @Override
    String getExecutable() {
      return "gnome-terminal";
    }
  },
  /**
   * The GNOME Console.
   */
  GNOME_CONSOLE {
    @Override
    String getExecutable() {
      return "kgx";
    }
  },
  /**
   * The KDE Konsole.
   */
  KDE_KONSOLE {
    @Override
    String getExecutable() {
      return "konsole";
    }
  },
  /**
   * The macOS Terminal.
   */
  MACOS_TERMINAL {
    @Override
    String getExecutable() {
      return "Terminal";
    }
  };

  abstract String getExecutable();

  /**
   * Wraps the given <code>cmdString</code> into an OS and terminal dependent command line.
   * The result can be passed to a {@link ProcessBuilder} to open a terminal and run the command,
   * allowing user input and showing process output.
   *
   * @param cmdString the command to be executed in the terminal
   * @param pwd       the directory the terminal should run in
   * @return the array of strings forming the final executed command by the terminal
   */
  public String[] wrapWithTerminalCommand(String cmdString, Path pwd) {
    return switch (this) {
      case WINDOWS_CMD -> new String[]{
          "cmd.exe",
          "/c",
          "start %s /c \"%s && pause || pause\"".formatted(getExecutable(), cmdString)
      };
      case WINDOWS_POWERSHELL -> new String[]{
          "cmd.exe",
          "/c",
          "start %s -Command \" %s ; pause \"".formatted(getExecutable(), cmdString.replace("\"", "'"))
      };
      case GNOME_TERMINAL, GNOME_CONSOLE -> new String[]{
          "/bin/sh",
          "-c",
          "%s -- bash -c \"%s; echo Done - Press enter to exit; read\"".formatted(getExecutable(), cmdString)
      };
      case KDE_KONSOLE -> new String[]{
          "/bin/sh",
          "-c",
          "%s -e bash -c \"%s; echo Done - Press enter to exit; read\"".formatted(getExecutable(), cmdString)
      };
      case MACOS_TERMINAL -> new String[]{
          "/bin/sh",
          "-c",
          "/usr/bin/osascript -e " +
              String.format(
                  "'tell app \"%s\" to activate & do script \"cd %s; %s; echo Done - Press enter to exit; read; exit\"'",
                  getExecutable(),
                  pwd,
                  cmdString.replace("\"", "")
              )
      };
    };
  }

  /**
   * Gets the available terminal application options, depending on the current OS.
   *
   * @return the array of available terminal applications
   */
  public static Terminal[] getSystemTerminalOptions() {
    return switch (OS.getOS()) {
      case WINDOWS -> new Terminal[]{WINDOWS_CMD, WINDOWS_POWERSHELL};
      case LINUX -> new Terminal[]{GNOME_TERMINAL, KDE_KONSOLE, GNOME_CONSOLE};
      case MAC -> new Terminal[]{MACOS_TERMINAL};
      case SOLARIS -> throw new RuntimeException("unsupported operating system");
    };
  }

  /**
   * Returns the default terminal application for the current operating system.
   *
   * @return the first entry of the available terminal applications
   */
  public static Terminal getDefaultSystemTerminal() {
    return getSystemTerminalOptions()[0];
  }

  /**
   * {@return the string representation name of the terminal application}
   */
  @Override
  public String toString() {
    return switch (this) {
      case WINDOWS_CMD -> "Windows Command Prompt";
      case WINDOWS_POWERSHELL -> "Windows PowerShell";
      case GNOME_TERMINAL -> "GNOME Terminal";
      case GNOME_CONSOLE -> "GNOME Console";
      case KDE_KONSOLE -> "KDE Konsole";
      case MACOS_TERMINAL -> "MacOS Terminal";
    };
  }
}
