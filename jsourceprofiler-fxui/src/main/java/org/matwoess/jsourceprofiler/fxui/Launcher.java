package org.matwoess.jsourceprofiler.fxui;

/**
 * Launcher class for the {@link App}.
 * Defining it and setting it as the main class reduces distribution artifact file sizes.
 */
public class Launcher {
  /**
   * Main method simply calling {@link App#main(String[])}.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    App.main(args);
  }
}
