package org.matwoess.jsourceprofiler.fxui.tree;

import org.matwoess.jsourceprofiler.fxui.model.AppState;
import org.matwoess.jsourceprofiler.fxui.util.DirectoryBeforeFileComparator;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This class is used to display the project directory as a tree view and interact with it.
 * It provides the possibility to select the sources directory and the main file as parameters.
 */
public class JavaProjectTree {
  /**
   * A comparator that sorts files alphabetically with directories before files.
   */
  private static final Comparator<File> treeComparator = new DirectoryBeforeFileComparator();
  private final AppState appState;

  /**
   * Creates a new JavaProjectTree.
   * <p>
   * The tree will be automatically populated with the project directory contents.
   * Directories not containing any ".java" files are filtered out, leaf notes are always ".java" files.
   *
   * @param appState       the app state to update when selecting files
   * @param treeProjectDir the FxUI view to render the tree in
   */
  public JavaProjectTree(AppState appState, TreeView<File> treeProjectDir) {
    this.appState = appState;
    initTreeView(treeProjectDir);
  }

  /**
   * Initializes the tree view with the project directory contents, sets up the context menu and the keypress listener.
   *
   * @param treeProjectDir the FxUI view the tree is rendered in
   */
  private void initTreeView(TreeView<File> treeProjectDir) {
    File rootDir = appState.projectRoot.get().toFile();
    TreeItem<File> root = populateTree(rootDir);
    treeProjectDir.setRoot(root);
    treeProjectDir.setShowRoot(false);
    treeProjectDir.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        selectTreeItem(treeProjectDir.getSelectionModel().getSelectedItem());
      }
    });
    initContextMenu(treeProjectDir);
    treeProjectDir.setCellFactory(tv -> new SelectableTreeCell(appState));
  }

  /**
   * Initializes the context menu for the tree view.
   * <p>
   * The context menu contains a single item
   * that allows to choose the selected tree item as the sources directory or the main file.
   *
   * @param treeProjectDir the FxUI view the tree is rendered in
   */
  private void initContextMenu(TreeView<File> treeProjectDir) {
    ContextMenu contextMenu = new ContextMenu();
    MenuItem selectItem = new MenuItem();
    selectItem.setOnAction(event -> selectTreeItem(treeProjectDir.getSelectionModel().getSelectedItem()));
    selectItem.textProperty().bind(Bindings.createStringBinding(
        () -> {
          TreeItem<File> item = treeProjectDir.getSelectionModel().getSelectedItem();
          if (item == null) return "";
          return item.getValue().isDirectory() ? "Select as sources root" : "Select as main file";
        },
        treeProjectDir.getSelectionModel().selectedItemProperty()
    ));
    contextMenu.getItems().add(selectItem);
    treeProjectDir.setContextMenu(contextMenu);
  }

  /**
   * Populates the tree with the contents of the given directory.
   * <p>
   * Directories not containing any ".java" files are filtered out, leaf notes are always ".java" files.
   *
   * @param directory the directory to populate the tree with
   * @return the populated tree
   */
  private TreeItem<File> populateTree(File directory) {
    File[] itemsInDir = directory.listFiles();
    TreeItem<File> folder = new TreeItem<>(directory);
    if (itemsInDir == null) return folder;
    Arrays.sort(itemsInDir, treeComparator);
    for (File item : itemsInDir) {
      if (item.isDirectory()) {
        TreeItem<File> subFolder = populateTree(item);
        if (!subFolder.getChildren().isEmpty()) {
          folder.getChildren().add(subFolder);
        }
      } else if (item.getName().endsWith(".java")) {
        folder.getChildren().add(new TreeItem<>(item));
      }
    }
    return folder;
  }

  /**
   * Selects the given tree item as the sources directory or the main file.
   * <p>
   * If the selected item is a directory, it is selected as the sources directory.
   * If the selected item is a file, it is selected as the main file.
   *
   * @param selected the selected tree item
   */
  private void selectTreeItem(TreeItem<File> selected) {
    if (selected == null) return;
    File value = selected.getValue();
    Path relPath = appState.projectRoot.get().relativize(value.toPath());
    if (value.isDirectory()) {
      appState.sourcesDir.set(relPath);
    } else {
      appState.mainFile.set(relPath);
    }
  }

}
