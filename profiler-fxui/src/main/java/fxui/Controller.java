package fxui;

import common.IO;
import fxui.model.Parameters;
import fxui.model.RunMode;
import fxui.util.SystemOutputTextFlowWriter;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import java.io.PrintStream;
import java.nio.file.Path;

public class Controller {

  @FXML
  private VBox vbMainFile;
  @FXML
  private TextField txtMainFile;
  @FXML
  private Button btnMainFile;
  @FXML
  private VBox vbProgramArgs;
  @FXML
  private TextField txtProgramArgs;
  @FXML
  private VBox vbSourcesDir;
  @FXML
  private TextField txtSourcesDir;
  @FXML
  private Button btnSourcesDir;
  @FXML
  private TextField txtOutputDir;
  @FXML
  private Button btnOutputDir;
  @FXML
  private HBox hbSyncCounters;
  @FXML
  private CheckBox cbSyncCounters;
  @FXML
  private CheckBox cbVerboseOutput;
  @FXML
  private Button btnOpenReport;
  @FXML
  private Button btnRunTool;
  @FXML
  private TextFlow txtFlowOutput;
  @FXML
  private ChoiceBox<RunMode> cbRunMode;

  private final Parameters parameters;

  public Controller() {
    parameters = new Parameters();
  }

  @FXML
  private void initialize() {
    bindParameters();
    setOnClickActions();
    initConsoleOutput();
    initRunModeControl();
    initDisabledPropertiesByMode();
    initButtonDisabledProperties();
    initBorderListeners();
    txtOutputDir.setPromptText(Path.of(".").resolve(IO.DEFAULT_OUT_DIR).toString());
  }

  private void bindParameters() {
    txtMainFile.textProperty().bindBidirectional(parameters.mainFile);
    txtProgramArgs.textProperty().bindBidirectional(parameters.programArgs);
    txtSourcesDir.textProperty().bindBidirectional(parameters.sourcesDir);
    txtOutputDir.textProperty().bindBidirectional(parameters.outputDir);
    cbVerboseOutput.selectedProperty().bindBidirectional(parameters.verboseOutput);
    cbSyncCounters.selectedProperty().bindBidirectional(parameters.syncCounters);
  }

  private void setOnClickActions() {
    btnMainFile.setOnAction(event -> PathUtils.chooseFile(txtMainFile));
    btnSourcesDir.setOnAction(event -> PathUtils.chooseDirectory(txtSourcesDir));
    btnOutputDir.setOnAction(event -> PathUtils.chooseDirectory(txtOutputDir));
  }

  private void initRunModeControl() {
    cbRunMode.getItems().setAll(RunMode.values());
    cbRunMode.valueProperty().bindBidirectional(parameters.runMode);
  }

  private void initConsoleOutput() {
    PrintStream consoleOutput = new PrintStream(new SystemOutputTextFlowWriter(txtFlowOutput));
    System.setOut(consoleOutput);
    System.setErr(consoleOutput);
  }

  private void initDisabledPropertiesByMode() {
    vbMainFile.disableProperty().bind(parameters.runMode.isEqualTo(RunMode.REPORT_ONLY));
    vbProgramArgs.disableProperty().bind(parameters.runMode.isNotEqualTo(RunMode.DEFAULT));
    vbSourcesDir.disableProperty().bind(parameters.runMode.isEqualTo(RunMode.REPORT_ONLY));
    hbSyncCounters.disableProperty().bind(parameters.runMode.isEqualTo(RunMode.REPORT_ONLY));
  }

  private void initButtonDisabledProperties() {
    btnOpenReport.disableProperty().bindBidirectional(parameters.invalidOutDirPath);
    BooleanBinding anyPathInvalid = parameters.invalidMainFilePath
        .or(parameters.invalidSourcesDirPath)
        .or(parameters.invalidOutDirPath);
    BooleanBinding instrumentWithoutTarget = parameters.runMode
        .isNotEqualTo(RunMode.REPORT_ONLY)
        .and(parameters.mainFile.isEmpty())
        .and(parameters.sourcesDir.isEmpty());
    btnRunTool.disableProperty().bind(anyPathInvalid.or(instrumentWithoutTarget));
  }

  private void initBorderListeners() {
    txtMainFile.borderProperty().bind(BindingUtils.createBorderBinding(parameters.mainFile, parameters.invalidMainFilePath));
    txtSourcesDir.borderProperty().bind(BindingUtils.createBorderBinding(parameters.sourcesDir, parameters.invalidSourcesDirPath));
    txtOutputDir.borderProperty().bind(BindingUtils.createBorderBinding(parameters.outputDir, parameters.invalidOutDirPath));
  }

  @FXML
  protected void onExecuteTool() {
    txtFlowOutput.getChildren().clear();
    tool.Main.main(parameters.getRunCommand());
  }

  @FXML
  protected void onOpenReport() {
    Path reportPath = IO.getReportIndexPath();
    PathUtils.openWithDesktopApplication(reportPath);
  }
}