package org.matwoess.jsourceprofiler.fxui.util;

import org.matwoess.jsourceprofiler.common.Util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Is used to watch a directory and all its subdirectories for file creation and deletion events.
 * It is used to inform listeners about changes to the output directory.
 */
public class RecursiveDirectoryWatcher {
  private final WatchService watcher;
  private final Map<WatchKey, Path> keys;
  private final FileEventListener listener;
  private final List<Path> watchedDirs;

  /**
   * Creates a new RecursiveDirectoryWatcher.
   * The watcher will monitor the given directory and all its subdirectories.
   * <p>
   * On any file creation or deletion event, the given listener will be informed.
   *
   * @param listener   the listener to inform about file creation and deletion events
   * @param rootDirectory the root directory to watch
   * @param watchedDirs the additional directories to watch
   * @throws IOException if any of the directories cannot be registered with the WatchService
   */
  public RecursiveDirectoryWatcher(FileEventListener listener, Path rootDirectory, Path... watchedDirs) throws IOException {
    this.watcher = FileSystems.getDefault().newWatchService();
    this.keys = new HashMap<>();
    this.listener = listener;
    this.watchedDirs = Arrays.asList(Util.prependToArray(watchedDirs, rootDirectory));
    registerAll(rootDirectory);
  }

  /**
   * Registers the given directory with the WatchService.
   *
   * @param dir the directory to register
   * @throws IOException if the registration fails
   */
  public void register(Path dir) throws IOException {
    WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    keys.put(key, dir);
  }

  /**
   * Registers the given directory and all its subdirectories with the WatchService.
   *
   * @param root the directory to register
   * @throws IOException if the registration fails
   */
  private void registerAll(final Path root) throws IOException {
    Files.walkFileTree(root, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (watchedDirs.contains(dir)) {
          register(dir);
          return FileVisitResult.CONTINUE;
        } else {
          return FileVisitResult.SKIP_SUBTREE;
        }
      }
    });
  }

  /**
   * Casts a WatchEvent of unknown type to a WatchEvent of the desired type.
   * @param event the event to cast
   * @return the event cast to the desired type
   * @param <T> the desired type
   */
  @SuppressWarnings("unchecked")
  static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  /**
   * Processes all events for keys queued to the watcher.
   */
  public void processEvents() {
    for (; ; ) {
      WatchKey key;
      try {
        key = watcher.take();
      } catch (InterruptedException x) {
        return;
      }
      Path dir = keys.get(key);
      if (dir == null) {
        System.err.println("WatchKey not recognized!");
        continue;
      }
      for (WatchEvent<?> event : key.pollEvents()) {
        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        Path child = dir.resolve(name);
        //System.out.format("%s: %s\n", event.kind().name(), child);
        WatchEvent.Kind<?> kind = event.kind();
        if (kind == OVERFLOW) {
          continue;
        }
        if (kind.equals(ENTRY_CREATE)) {
          if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
            try {
              registerAll(child);
            } catch (IOException ignored) {
            }
          }
          listener.onFileCreated(child);
        } else if (kind.equals(ENTRY_DELETE)) {
          listener.onFileDeleted(child);
        }
      }
      boolean valid = key.reset();
      if (!valid) {
        keys.remove(key);
        if (keys.isEmpty()) {
          break;
        }
      }
    }
  }

  /**
   * An interface for listeners that want to be informed about file creation and deletion events.
   */
  public interface FileEventListener {
    /**
     * Called when a file is created.
     * @param path the path to the created file
     */
    void onFileCreated(@SuppressWarnings("unused") Path path);

    /**
     * Called when a file is deleted.
     * @param path the path to the deleted file
     */
    void onFileDeleted(@SuppressWarnings("unused") Path path);
  }
}
