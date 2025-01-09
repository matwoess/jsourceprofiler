package org.matwoess.jsourceprofiler.fxui.util;

import java.io.File;
import java.util.Comparator;

/**
 * This comparator sorts files alphabetically with directories before files.
 */
public class DirectoryBeforeFileComparator implements Comparator<File> {
  @Override
  public int compare(File item1, File item2) {
    if (item1.isDirectory() && item2.isFile()) return -1;
    if (item2.isDirectory() && item1.isFile()) return 1;
    return item1.getName().compareTo(item2.getName());
  }
}
