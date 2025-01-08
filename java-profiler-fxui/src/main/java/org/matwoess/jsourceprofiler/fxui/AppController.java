package org.matwoess.jsourceprofiler.fxui;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.common.Util;
import org.matwoess.jsourceprofiler.fxui.model.AppState;
import org.matwoess.jsourceprofiler.common.RunMode;
import org.matwoess.jsourceprofiler.fxui.model.Terminal;
import org.matwoess.jsourceprofiler.fxui.tree.JavaProjectTree;
import org.matwoess.jsourceprofiler.fxui.util.BindingUtils;
import org.matwoess.jsourceprofiler.fxui.util.RecursiveDirectoryWatcher;
import org.matwoess.jsourceprofiler.fxui.util.SystemUtils;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * The controller for the main application window.
 */
public class AppController implements RecursiveDirectoryWatcher.FileEventListener {

  private Stage applicationStage;

  @FXML
  private TreeView<File> treeProjectDir;
  @FXML
  private ChoiceBox<RunMode> cbRunMode;
  @FXML
  private VBox boxSourcesDir;
  @FXML
  private TextField txtSourcesDir;
  @FXML
  private Button btnClearSourcesDir;
  @FXML
  private VBox boxMainFile;
  @FXML
  private TextField txtMainFile;
  @FXML
  private Label requiredHintMainFile;
  @FXML
  private Button btnClearMainFile;
  @FXML
  private VBox boxProgramArgs;
  @FXML
  private TextField txtProgramArgs;
  @FXML
  private HBox boxSyncCounters;
  @FXML
  private CheckBox cbSyncCounters;
  @FXML
  private ChoiceBox<Terminal> cbTerminalEmulator;
  @FXML
  private TextField txtJavaVersion;
  @FXML
  private Button btnCommandPreview;
  @FXML
  private Button btnOpenReport;
  @FXML
  private Button btnRunTool;
  @FXML
  private Button btnRestoreParameters;

  private final AppState appState;

  private JavaProjectTree projectTree;
  private RecursiveDirectoryWatcher recursiveDirectoryWatcher;

  private final static String JAVA_VERSION_NOT_RECOGNIZED = "Unable to determine";

  /**
   * Creates a new AppController and initializes the app state.
   */
  public AppController() {
    appState = new AppState();
  }

  /**
   * Initializes the UI and control bindings of the main application window.
   */
  @FXML
  private void initialize() {
    bindParameters();
    initRunModeControl();
    initTerminalEmulatorControl();
    initDisabledPropertiesByMode();
    initButtonProperties();
    initRunToolButton();
    initBorderListeners();
    initRecognizedJavaVersionControl();
  }

  /**
   * Initializes the stage of the main application window.
   *
   * @param stage the stage to initialize
   */
  void initUI(Stage stage) {
    applicationStage = stage;
    applicationStage.setResizable(false);
    applicationStage.setTitle("Java Profiler");
  }

  /**
   * Sets the project root path and initializes the project tree.
   * <p>
   * Additionally, the directory watcher is initialized.
   *
   * @param projectRootPath the project root path of the opened project
   */
  public void setProjectDirectory(Path projectRootPath) {
    IO.outputDir = projectRootPath.resolve(IO.outputDir);
    appState.projectRoot.set(projectRootPath);
    applicationStage.setTitle(applicationStage.getTitle() + " - " + projectRootPath);
    projectTree = new JavaProjectTree(appState, treeProjectDir);
    initDirectoryWatcher(projectRootPath, IO.getOutputDir());
  }

  /**
   * Initializes the directory watcher for the given project root path and output directory.
   * <p>
   * The project root path is needed in case the output directory is not created yet.
   */
  private void initDirectoryWatcher(Path projectRoot, Path outputDir) {
    try {
      recursiveDirectoryWatcher = new RecursiveDirectoryWatcher(this, projectRoot, outputDir);
      Thread watcherThread = new Thread(() -> recursiveDirectoryWatcher.processEvents());
      watcherThread.setDaemon(true);
      watcherThread.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Binds the properties of the app state to the UI controls.
   */
  private void bindParameters() {
    txtMainFile.textProperty().bindBidirectional(appState.mainFile, BindingUtils.pathStringConverter);
    txtProgramArgs.textProperty().bindBidirectional(appState.programArgs);
    txtSourcesDir.textProperty().bindBidirectional(appState.sourcesDir, BindingUtils.pathStringConverter);
    cbSyncCounters.selectedProperty().bindBidirectional(appState.syncCounters);
  }

  /**
   * Initializes the run mode control.
   */
  private void initRunModeControl() {
    cbRunMode.getItems().setAll(RunMode.values());
    cbRunMode.valueProperty().bindBidirectional(appState.runMode);
  }

  /**
   * Initializes the terminal emulator control.
   */
  private void initTerminalEmulatorControl() {
    cbTerminalEmulator.getItems().setAll(Terminal.getSystemTerminalOptions());
    cbTerminalEmulator.valueProperty().bindBidirectional(appState.terminal);
  }

  /**
   * Initializes the disabled state properties for all the UI controls depending on the run mode.
   */
  private void initDisabledPropertiesByMode() {
    boxMainFile.visibleProperty().bind(appState.runMode.isNotEqualTo(RunMode.REPORT_ONLY));
    boxProgramArgs.visibleProperty().bind(appState.runMode.isEqualTo(RunMode.DEFAULT));
    boxProgramArgs.managedProperty().bind(boxProgramArgs.visibleProperty());
    boxSourcesDir.visibleProperty().bind(appState.runMode.isNotEqualTo(RunMode.REPORT_ONLY));
    boxSyncCounters.visibleProperty().bind(appState.runMode.isNotEqualTo(RunMode.REPORT_ONLY));
    requiredHintMainFile.visibleProperty().bind(appState.runMode.isEqualTo(RunMode.DEFAULT));
    requiredHintMainFile.managedProperty().bind(requiredHintMainFile.visibleProperty());
  }

  /**
   * Initializes the visibility properties of button controls.
   */
  private void initButtonProperties() {
    btnClearSourcesDir.visibleProperty().bind(appState.sourcesDir.isNotNull());
    btnClearSourcesDir.managedProperty().bind(btnClearSourcesDir.visibleProperty());
    btnClearMainFile.visibleProperty().bind(appState.mainFile.isNotNull());
    btnClearMainFile.managedProperty().bind(btnClearMainFile.visibleProperty());
    btnRestoreParameters.visibleProperty().bind(appState.parametersFileExists);
    btnOpenReport.visibleProperty().bind(appState.reportIndexFileExists);
  }

  /**
   * Initializes the "Run tool" and "Preview command" button properties.
   */
  private void initRunToolButton() {
    BooleanBinding anyPathInvalid = appState.invalidMainFilePath.or(appState.invalidSourcesDirPath);
    BooleanBinding instrumentWithoutTarget = appState.runMode
        .isNotEqualTo(RunMode.REPORT_ONLY)
        .and(appState.mainFile.isNull())
        .and(appState.sourcesDir.isNull());
    BooleanBinding runWithoutMainFile = appState.runMode
        .isEqualTo(RunMode.DEFAULT)
        .and(appState.mainFile.isNull());
    BooleanBinding generateWithMissingData = appState.runMode
        .isEqualTo(RunMode.REPORT_ONLY)
        .and(appState.metadataFileExists.not().or(appState.countsFileExists.not()));
    btnRunTool.disableProperty().bind(
        anyPathInvalid.or(instrumentWithoutTarget).or(runWithoutMainFile).or(generateWithMissingData)
    );
    btnCommandPreview.disableProperty().bind(btnRunTool.disableProperty());
  }

  /**
   * Init the listeners for the border properties of the main file and sources directory text fields.
   */
  private void initBorderListeners() {
    txtMainFile.borderProperty().bind(BindingUtils.createBorderBinding(appState.mainFile, appState.invalidMainFilePath));
    txtSourcesDir.borderProperty().bind(BindingUtils.createBorderBinding(appState.sourcesDir, appState.invalidSourcesDirPath));
  }

  /**
   * Empties the sources directory path property.
   */
  public void onClearSourcesDir() {
    appState.sourcesDir.set(null);
  }

  /**
   * Empties the main file path property.
   */
  public void onClearMainFile() {
    appState.mainFile.set(null);
  }

  /**
   * Executes the tool with the given parameters (in the system terminal).
   */
  @FXML
  protected void onExecuteTool() {
    int exitCode = SystemUtils.executeToolWithParameters(appState);
    if (exitCode != 0) {
      throw new RuntimeException("error executing tool");
    }
  }

  /**
   * Opens the report index file in the default desktop application.
   */
  @FXML
  protected void onOpenReport() {
    Path reportPath = IO.getReportIndexPath();
    SystemUtils.openWithDesktopApplication(reportPath);
  }

  /**
   * Shows and initializes the command preview modal dialog.
   *
   * @throws IOException if loading of the FXML fails
   */
  @FXML
  protected void showRunCommand() throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(ProjectController.class.getResource("command-view.fxml"));
    Stage cmdStage = new Stage();
    cmdStage.setScene(new Scene(fxmlLoader.load()));
    cmdStage.initOwner(applicationStage);
    cmdStage.initModality(Modality.APPLICATION_MODAL);
    CommandController cmdController = fxmlLoader.getController();
    cmdController.initUI(cmdStage);
    cmdController.setRunCommand(SystemUtils.getJavaRunCommand(appState.getProgramArguments()));
    cmdStage.showAndWait();
  }

  /**
   * Initializes the recognized Java version control.
   */
  private void initRecognizedJavaVersionControl() {
    Platform.runLater(() -> {
      String[] command = new String[]{"java", "-version"};
      String result = JAVA_VERSION_NOT_RECOGNIZED;
      try {
        String[] output = Util.runCommandAndGetOutput(appState.projectRoot.get(), command);
        if (output.length > 0) {
          result = output[0];
        }
      } catch (Exception ignored) {
      }
      txtJavaVersion.textProperty().set(result);
    });
  }

  /**
   * Triggers a full rebuild of the project tree.
   */
  @FXML
  protected void onRebuildTree() {
    projectTree = new JavaProjectTree(appState, treeProjectDir);
  }

  /**
   * Export the current parameters to the filesystem.
   */
  @FXML
  protected void onSaveParameters() {
    appState.exportParameters();
  }

  /**
   * Import the parameters from the filesystem.
   */
  @FXML
  protected void onRestoreParameters() {
    appState.importParameters();
  }

  /**
   * Handles the file creation event of the directory watcher.
   *
   * @param path the path to the created file
   */
  @Override
  public void onFileCreated(Path path) {
    appState.invalidateFileBindings();
  }

  /**
   * Handles the path deletion event of the directory watcher.
   *
   * @param path the path to the deleted file
   */
  @Override
  public void onFileDeleted(Path path) {
    appState.invalidateFileBindings();
  }
}