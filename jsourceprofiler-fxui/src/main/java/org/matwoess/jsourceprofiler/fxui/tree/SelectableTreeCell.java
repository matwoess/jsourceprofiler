package org.matwoess.jsourceprofiler.fxui.tree;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.fxui.model.AppState;
import org.matwoess.jsourceprofiler.fxui.util.BindingUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Helper class for the JavaProjectTree.
 * <p>
 * Provides custom cell rendering for tree items, based on whether they match selected or relevant paths.
 * Further, it shows custom icons for directories and java files.
 */
class SelectableTreeCell extends TreeCell<File> {

  private static final Image folderIcon = new Image(Objects.requireNonNull(JavaProjectTree.class.getResourceAsStream("folder-icon.png")));
  private static final Image jFileIcon = new Image(Objects.requireNonNull(JavaProjectTree.class.getResourceAsStream("java-icon.png")));

  private static final Background selectedItemColor = Background.fill(Color.color(.9, .9, .9, .15));
  private static final Background outDirColor = Background.fill(Color.color(.9, .4, .1, .2));
  private static final Background outDirSelColor = Background.fill(Color.color(.9, .4, .1, .3));
  private static final Background srcDirColor = Background.fill(Color.color(.1, .2, .8, .2));
  private static final Background srcDirSelColor = Background.fill(Color.color(.1, .2, .8, .3));
  private static final Background mainFileColor = Background.fill(Color.color(.1, .5, .1, .2));
  private static final Background mainFileSelColor = Background.fill(Color.color(.1, .5, .1, .3));


  /**
   * Creates a new SelectableTreeCell and sets up all bindings for the cell.
   * @param appState the app state used for the bindings
   */
  public SelectableTreeCell(AppState appState) {
    BooleanBinding isSelectedDir = Bindings.createBooleanBinding(
        () -> {
          Path srcDir = appState.sourcesDir.get();
          if (srcDir == null) return false;
          TreeItem<File> item = this.getTreeItem();
          if (item == null) return false;
          File dir = item.getValue();
          if (dir == null) return false;
          return appState.projectRoot.get().resolve(srcDir).equals(dir.toPath());
        },
        treeItemProperty(), appState.sourcesDir
    );
    BooleanBinding isSelectedMain = Bindings.createBooleanBinding(
        () -> {
          Path mainFile = appState.mainFile.get();
          if (mainFile == null) return false;
          TreeItem<File> item = this.getTreeItem();
          if (item == null) return false;
          File file = item.getValue();
          if (file == null) return false;
          return appState.projectRoot.get().resolve(mainFile).equals(file.toPath());
        },
        treeItemProperty(), appState.mainFile
    );
    BooleanBinding isOutDir = Bindings.createBooleanBinding(
        () -> {
          TreeItem<File> item = this.getTreeItem();
          if (item == null) return false;
          File file = item.getValue();
          if (file == null) return false;
          return appState.projectRoot.get().resolve(IO.getOutputDir()).equals(file.toPath());
        },
        treeItemProperty()
    );
    graphicProperty().bind(Bindings.createObjectBinding(
            this::getItemGraphic,
            itemProperty()
        )
    );
    backgroundProperty().bind(Bindings.createObjectBinding(
        () -> getItemBackgroundColor(isOutDir.get(), isSelectedDir.get(), isSelectedMain.get()),
        isSelectedDir, isSelectedMain, selectedProperty(), itemProperty()
    ));
    borderProperty().bind(BindingUtils.createSelectedTreeItemBorderBinding(isOutDir, isSelectedDir, isSelectedMain));
  }

  /**
   * Updates the item text.
   * @param item the item to update the text for
   * @param empty whether the item is empty
   */
  @Override
  protected void updateItem(File item, boolean empty) {
    super.updateItem(item, empty);
    setText((empty || item == null) ? "" : item.getName());
  }

  /**
   * Returns the graphic for the tree item.
   * <p>
   * The graphic is either a folder icon or a java file icon, depending on the item type.
   * @return the graphic for the tree item
   */
  private ImageView getItemGraphic() {
    if (itemProperty().isNotNull().get()) {
      return itemProperty().get().isDirectory() ? new ImageView(folderIcon) : new ImageView(jFileIcon);
    }
    return null;
  }

  /**
   * Returns the background color for the tree item.
   * <p>
   * The background color depends on whether the item is the output directory, the sources directory or the main file.
   * Further, the background color depends on whether the item is selected.
   * @param isOutDir whether the item is the output directory
   * @param isSelectedDir whether the item is the sources directory
   * @param isSelectedMain whether the item is the main file
   * @return the background color for the tree item
   */
  private Background getItemBackgroundColor(boolean isOutDir, boolean isSelectedDir, boolean isSelectedMain) {
    if (isOutDir) {
      return isSelected() ? outDirSelColor : outDirColor;
    }
    if (isSelectedDir) {
      return isSelected() ? srcDirSelColor : srcDirColor;
    }
    if (isSelectedMain) {
      return isSelected() ? mainFileSelColor : mainFileColor;
    }
    if (isSelected()) {
      return selectedItemColor;
    }
    return null;
  }
}
