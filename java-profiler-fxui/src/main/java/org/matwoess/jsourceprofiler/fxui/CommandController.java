package org.matwoess.jsourceprofiler.fxui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controller for the run command preview dialog.
 */
public class CommandController {
  Stage dialogStage;

  @FXML
  private TextArea txtRunCommand;
  @FXML
  private Button btnCopyToClipboard;

  /**
   * Border to be set when the copy button is clicked.
   */
  private static final Border clickedBorder = new Border(
      new BorderStroke(
          Color.GREEN,
          BorderStrokeStyle.SOLID,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );


  /**
   * Initializes the UI of the dialog.
   *
   * @param stage the stage of the dialog
   */
  public void initUI(Stage stage) {
    this.dialogStage = stage;
    stage.setTitle("Run Command Preview");
    stage.getScene().setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
  }

  /**
   * Sets the run command to be displayed in the dialog.
   *
   * @param runCommand the run command to be displayed
   */
  public void setRunCommand(String runCommand) {
    txtRunCommand.textProperty().set(runCommand);
  }

  /**
   * Copies the shown run command to the system clipboard.
   */
  @FXML
  private void copyCommandToClipboard() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(txtRunCommand.textProperty().get());
    clipboard.setContent(content);
    btnCopyToClipboard.borderProperty().set(clickedBorder);
  }

  /**
   * Closes the dialog by closing the stage.
   */
  @FXML
  private void closeDialog() {
    dialogStage.close();
  }
}
