package org.matwoess.jsourceprofiler.fxui;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.fxui.util.BindingUtils;
import org.matwoess.jsourceprofiler.fxui.util.SystemUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The controller for the project selection dialog.
 */
public class ProjectController {
  public Stage projectStage;

  @FXML
  private Button btnProjectRoot;
  @FXML
  private TextField txtProjectRoot;
  @FXML
  private Button btnOpenProject;

  public BooleanBinding invalidProjectRootPath;

  /**
   * Initializes the UI of the project selection dialog.
   *
   * @param stage the stage of the dialog
   */
  public void initUI(Stage stage) {
    projectStage = stage;
    projectStage.setResizable(false);
    projectStage.setTitle("Open Java Project");
  }

  /**
   * Initializes the properties and bindings of the project selection dialog.
   * <p>
   * Additionally, the last project root path is restored using {@link #restoreLastProjectPath}.
   * @param projectRootProperty the property to be set with the selected project root path
   */
  void initProperties(ObjectProperty<Path> projectRootProperty) {
    txtProjectRoot.textProperty().bindBidirectional(projectRootProperty, BindingUtils.pathStringConverter);
    invalidProjectRootPath = txtProjectRoot.textProperty()
        .isNotEmpty()
        .and(BindingUtils.createIsDirectoryBinding(projectRootProperty).not());
    btnProjectRoot.setOnAction(event -> onPickProjectRoot(projectRootProperty));
    txtProjectRoot.borderProperty().bind(
        BindingUtils.createBorderBinding(txtProjectRoot.textProperty(), invalidProjectRootPath));
    btnOpenProject.disableProperty().bind(txtProjectRoot.textProperty().isEmpty().or(invalidProjectRootPath));
    restoreLastProjectPath();
  }

  /**
   * Opens a directory chooser dialog and sets the chosen path on the given project root property.
   * @param projectRootProperty the project root property to set
   */
  private void onPickProjectRoot(ObjectProperty<Path> projectRootProperty) {
    SystemUtils.chooseDirectory(projectRootProperty);
  }

  /**
   * Closes the project selection dialog and exports the selected project root path using {@link #exportProjectPath}.
   */
  public void onOpenProject() {
    exportProjectPath();
    projectStage.close();
  }

  /**
   * Persists the selected project root path to the file system.
   */
  private void exportProjectPath() {
    try {
      Files.writeString(IO.lastProjectPath(), txtProjectRoot.textProperty().get());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Restores the last project root path from the file system.
   */
  private void restoreLastProjectPath() {
    if (!IO.lastProjectPath().toFile().exists()) return;
    try {
      String lastProjectPathStr = Files.readString(IO.lastProjectPath());
      txtProjectRoot.textProperty().set(lastProjectPathStr);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
