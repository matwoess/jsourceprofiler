package org.matwoess.jsourceprofiler.fxui;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The main class of the application extending {@link Application}.
 * <p>
 * Launches the application and initializes the project selection dialog and main window.
 */
public class App extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Starts the application.
   * <p>
   * First, the project selection dialog is shown.
   * After the user has selected a project directory,
   * the main application window is displayed.
   *
   * @param primaryStage the primary stage for this application
   * @throws Exception if an error occurs
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
    ObjectProperty<Path> projectRootProperty = new SimpleObjectProperty<>();
    showProjectRootDialog(projectRootProperty);
    showMainApp(primaryStage, projectRootProperty);
  }

  /**
   * Shows the dialog for selecting a project directory to open.
   *
   * @param projectRootProperty the path property to be set after selection
   * @throws IOException if loading of the FXML fails
   */
  private void showProjectRootDialog(ObjectProperty<Path> projectRootProperty) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(ProjectController.class.getResource("project-view.fxml"));
    Stage projectStage = new Stage();
    projectStage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("logo.png"))));
    projectStage.setScene(new Scene(fxmlLoader.load()));
    ProjectController prjController = fxmlLoader.getController();
    prjController.initProperties(projectRootProperty);
    prjController.initUI(projectStage);
    projectStage.setOnCloseRequest((e) -> {
      Platform.exit();
      System.exit(0);
    });
    projectStage.showAndWait();
  }

  /**
   * Shows the main application window.
   *
   * @param primaryStage        the stage for the application
   * @param projectRootProperty the project directory property chosen by the project root dialog
   * @throws IOException if loading of the FXML fails
   */
  private void showMainApp(Stage primaryStage, ObjectProperty<Path> projectRootProperty) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("app-view.fxml"));
    primaryStage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("logo.png"))));
    primaryStage.setScene(new Scene(fxmlLoader.load()));
    AppController appController = fxmlLoader.getController();
    appController.initUI(primaryStage);
    appController.setProjectDirectory(projectRootProperty.get());
    primaryStage.show();
  }
}