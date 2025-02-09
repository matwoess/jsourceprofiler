package org.matwoess.jsourceprofiler.fxui.model;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.common.RunMode;
import org.matwoess.jsourceprofiler.fxui.util.BindingUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.matwoess.jsourceprofiler.common.RunMode.DEFAULT;
import static org.matwoess.jsourceprofiler.common.RunMode.REPORT_ONLY;

/**
 * This class is used to store the state of the main FxUI app window.
 * <p>
 * It is used to store parameters like the project root, the run mode,
 * the sources directory, the main file, program arguments,
 * whether counters should be inserted synchronized and in which system terminal the tool should be executed in.
 * <p>
 * It also contains bindings for the validity of chosen files and the existence of relevant paths like the
 * report index file, the parameters file, the metadata and the counts file.
 */
public class AppState {
  public final ObjectProperty<Path> projectRoot = new SimpleObjectProperty<>(null);

  public final ObjectProperty<RunMode> runMode = new SimpleObjectProperty<>(DEFAULT);

  public final ObjectProperty<Path> sourcesDir = new SimpleObjectProperty<>(null);
  public final ObjectProperty<Path> mainFile = new SimpleObjectProperty<>(null);
  public final StringProperty programArgs = new SimpleStringProperty("");
  public final BooleanProperty syncCounters = new SimpleBooleanProperty(false);
  public final ObjectProperty<Terminal> terminal = new SimpleObjectProperty<>(Terminal.getDefaultSystemTerminal());

  /**
   * Helper binding telling us whether the currently specified main file path is invalid.
   */
  public BooleanBinding invalidMainFilePath;
  /**
   * Helper binding telling us whether the current sources directory path is invalid.
   */
  public BooleanBinding invalidSourcesDirPath;
  /**
   * Helper binding telling us whether the report index file exists.
   */
  public BooleanBinding reportIndexFileExists;
  /**
   * Helper binding telling us whether the parameters file exists.
   */
  public BooleanBinding parametersFileExists;
  /**
   * Helper binding telling us whether the metadata file exists.
   */
  public BooleanBinding metadataFileExists;
  /**
   * Helper binding telling us whether the counts file exists.
   */
  public BooleanBinding countsFileExists;

  public AppState() {
    initializeAdditionalProperties();
  }

  /**
   * Initializes all bindings that depend on the output directory (relative to the project root).
   */
  private void initializeAdditionalProperties() {
    invalidMainFilePath = mainFile.isNotNull().and(BindingUtils.creatRelativeIsJavaFileBinding(projectRoot, mainFile).not());
    invalidSourcesDirPath = sourcesDir.isNotNull().and(BindingUtils.createRelativeIsDirectoryBinding(projectRoot, sourcesDir).not());
    reportIndexFileExists = BindingUtils.creatRelativeFileExistsBinding(projectRoot, IO.getReportIndexPath());
    parametersFileExists = BindingUtils.creatRelativeFileExistsBinding(projectRoot, IO.getUIParametersPath());
    metadataFileExists = BindingUtils.creatRelativeFileExistsBinding(projectRoot, IO.getMetadataPath());
    countsFileExists = BindingUtils.creatRelativeFileExistsBinding(projectRoot, IO.getCountsPath());
  }

  /**
   * Invalidates all bindings that depend on the output directory (relative to the project root).
   * Is called when files are added or removed from the output directory.
   */
  public void invalidateFileBindings() {
    reportIndexFileExists.invalidate();
    metadataFileExists.invalidate();
    countsFileExists.invalidate();
    parametersFileExists.invalidate();
  }

  /**
   * Returns the arguments that should be passed to the tool.
   * <p>
   * The arguments depend on the run mode and the chosen parameters.
   *
   * @return an array of the arguments that will be passed to the tool
   */
  public String[] getProgramArguments() {
    RunMode mode = runMode.get();
    List<String> arguments = new ArrayList<>();
    boolean sync = syncCounters.get();
    if (sync && mode != REPORT_ONLY) {
      arguments.add("--synchronized");
    }
    switch (mode) {
      case REPORT_ONLY -> arguments.add("--generate-report");
      case INSTRUMENT_ONLY -> {
        arguments.add("--instrument-only");
        arguments.add(mainFile.get().toString());
      }
      case DEFAULT -> {
        if (sourcesDir.isNotNull().get()) {
          String additionalSourcesDir = sourcesDir.get().toString();
          if (!additionalSourcesDir.isBlank()) {
            arguments.add("--sources-directory");
            arguments.add(additionalSourcesDir);
          }
        }
        arguments.add(mainFile.get().toString());
        String args = programArgs.get();
        if (!args.isBlank()) {
          arguments.addAll(Arrays.stream(args.split(" ")).toList());
        }
      }
    }
    return arguments.toArray(String[]::new);
  }

  /**
   * Exports the currently configured parameters to the <code>parameters.dat</code> file (in the output directory).
   */
  public void exportParameters() {
    Path parametersPath = IO.getUIParametersPath();
    IO.createDirectoriesIfNotExists(parametersPath);
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(parametersPath.toFile()))) {
      oos.writeInt(runMode.get().ordinal());
      oos.writeUTF(sourcesDir.get() != null ? sourcesDir.get().toString() : "");
      oos.writeUTF(mainFile.get() != null ? mainFile.get().toString() : "");
      oos.writeUTF(programArgs.get());
      oos.writeBoolean(syncCounters.get());
      oos.writeInt(terminal.get().ordinal());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Imports and replaces the currently configured parameters from the <code>parameters.dat</code> file.
   */
  public void importParameters() {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(IO.getUIParametersPath().toFile()))) {
      runMode.set(RunMode.values()[ois.readInt()]);
      String sourcesDirVal = ois.readUTF();
      sourcesDir.set(sourcesDirVal.isBlank() ? null : Path.of(sourcesDirVal));
      String mainFileVal = ois.readUTF();
      mainFile.set(mainFileVal.isBlank() ? null : Path.of(mainFileVal));
      programArgs.set(ois.readUTF());
      syncCounters.set(ois.readBoolean());
      terminal.set(Terminal.values()[ois.readInt()]);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
