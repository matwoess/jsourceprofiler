package fxui.model;

import fxui.BindingUtils;
import javafx.beans.property.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fxui.model.RunMode.*;

public class Parameters implements Serializable {
  public ObjectProperty<RunMode> runMode = new SimpleObjectProperty<>(DEFAULT);

  public StringProperty mainFile = new SimpleStringProperty("");
  public StringProperty programArgs = new SimpleStringProperty("");
  public StringProperty sourcesDir = new SimpleStringProperty("");
  public StringProperty outputDir = new SimpleStringProperty("");
  public BooleanProperty syncCounters = new SimpleBooleanProperty(false);
  public BooleanProperty verboseOutput = new SimpleBooleanProperty(false);

  public BooleanProperty invalidMainFilePath = new SimpleBooleanProperty(false);
  public BooleanProperty invalidOutDirPath = new SimpleBooleanProperty(false);
  public BooleanProperty invalidSourcesDirPath = new SimpleBooleanProperty(false);

  public Parameters() {
    initializeExtraProperties();
  }

  public void initializeExtraProperties() {
    invalidMainFilePath.bind(mainFile.isNotEmpty().and(BindingUtils.createIsJavaFileBinding(mainFile).not()));
    invalidSourcesDirPath.bind(sourcesDir.isNotEmpty().and(BindingUtils.createIsDirectoryBinding(sourcesDir).not()));
    invalidOutDirPath.bind(outputDir.isNotEmpty().and(BindingUtils.createIsDirectoryBinding(outputDir).not()));
  }

  public String[] getRunCommand() {
    RunMode mode = runMode.get();
    List<String> arguments = new ArrayList<>();
    String outDir = outputDir.get();
    if (!outDir.isBlank()) {
      arguments.add("--out-directory");
      arguments.add(outDir);
    }
    boolean verbose = verboseOutput.get();
    if (verbose) {
      arguments.add("--verbose");
    }
    boolean sync = syncCounters.get();
    if (sync && mode != REPORT_ONLY) {
      arguments.add("--sync-counters");
    }
    switch (mode) {
      case REPORT_ONLY -> arguments.add("--generate-report");
      case INSTRUMENT_ONLY -> {
        arguments.add("--instrument-only");
        arguments.add(mainFile.get());
      }
      case DEFAULT -> {
        String additionalSourcesDir = sourcesDir.get();
        if (!additionalSourcesDir.isBlank()) {
          arguments.add("--sources-directory");
          arguments.add(additionalSourcesDir);
        }
        arguments.add(mainFile.get());
        String args = programArgs.get();
        if (!args.isBlank()) {
          arguments.addAll(Arrays.stream(args.split(" ")).toList());
        }
      }
    }
    return arguments.toArray(String[]::new);
  }
}
