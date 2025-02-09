package org.matwoess.jsourceprofiler.common;

/**
 * Helper enum class for operating system dependent get actions.
 */
public enum OS {
  /**
   * The Windows operating system.
   */
  WINDOWS,
  /**
   * The Linux operating system.
   */
  LINUX,
  /**
   * The macOS operating system.
   */
  MAC,
  /**
   * The Solaris operating system.
   */
  SOLARIS;

  // persist OS in variable for faster access next time
  private static OS os = null;

  /**
   * {@return the operating system's default line separator characters}
   */
  public String lineSeparator() {
    if (this == WINDOWS) {
      return "\r\n";
    } else {
      return "\n";
    }
  }

  /**
   * {@return the operating system's default class path separator character}
   */
  public String pathSeparator() {
    if (this == WINDOWS) {
      return ";";
    } else {
      return ":";
    }
  }

  /**
   * Use the system property <code>os.name</code> to determine the current operating system.
   *
   * @return the enum value associated to the system that the program is run on
   */
  public static OS getOS() {
    if (os == null) {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.contains("win")) {
        os = OS.WINDOWS;
      } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
        os = OS.LINUX;
      } else if (osName.contains("mac")) {
        os = OS.MAC;
      } else if (osName.contains("sunos")) {
        os = OS.SOLARIS;
      }
    }
    return os;
  }
}
