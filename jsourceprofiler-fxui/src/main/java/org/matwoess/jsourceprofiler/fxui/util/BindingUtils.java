package org.matwoess.jsourceprofiler.fxui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.nio.file.Path;

/**
 * This class contains utility methods for creating bindings.
 */
public class BindingUtils {
  private static final Border invalidBorder = new Border(
      new BorderStroke(
          Color.RED,
          BorderStrokeStyle.DASHED,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );
  private static final Border validBorder = new Border(
      new BorderStroke(
          Color.GREEN,
          BorderStrokeStyle.SOLID,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );
  private static final Border srcDirBorder = new Border(
      new BorderStroke(
          Color.BLUE,
          BorderStrokeStyle.SOLID,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );
  private static final Border mainFileBorder = validBorder;
  private static final Border outDirBorder = new Border(
      new BorderStroke(
          Color.BROWN,
          BorderStrokeStyle.SOLID,
          CornerRadii.EMPTY,
          BorderWidths.DEFAULT
      )
  );
  private static final Border neutralBorder = null;

  /**
   * This StringConverter is used to convert a Path to a String and vice versa.
   */
  public static final StringConverter<Path> pathStringConverter = new StringConverter<>() {
    @Override
    public String toString(Path object) {
      return object != null ? object.toString() : "";
    }

    @Override
    public Path fromString(String string) {
      return Path.of(string);
    }
  };

  /**
   * Specialized version of {@link #createBorderBinding(ObjectProperty, BooleanBinding)} for StringProperties.
   * <p>
   * The neutral border is returned if the property is empty (not null).
   *
   * @param stringProperty    the property to create the binding for
   * @param invalidityBinding the binding that determines whether the property is invalid
   * @return the created border binding
   */
  public static ObjectBinding<Border> createBorderBinding(StringProperty stringProperty, BooleanBinding invalidityBinding) {
    return Bindings.createObjectBinding(
        () -> {
          if (stringProperty.isEmpty().get()) return neutralBorder;
          if (invalidityBinding.get()) return invalidBorder;
          else return validBorder;
        },
        stringProperty,
        invalidityBinding
    );
  }

  /**
   * Creates a binding that returns a Border depending on the validity of the property.
   * <p>
   * If the property is null, the neutral border is returned.
   * If the invalidity-binding is true, the invalid border is returned.
   * Otherwise, the valid border is returned.
   * <p>
   * The binding is invalidated when the property or the invalidity-binding changes.
   *
   * @param property          the property to check
   * @param invalidityBinding the binding that determines whether the property is invalid
   * @param <T>               the generic type of the property
   * @return the created binding
   */
  public static <T> ObjectBinding<Border> createBorderBinding(ObjectProperty<T> property, BooleanBinding invalidityBinding) {
    return Bindings.createObjectBinding(
        () -> {
          if (property.isNull().get()) return neutralBorder;
          if (invalidityBinding.get()) return invalidBorder;
          else return validBorder;
        },
        property,
        invalidityBinding
    );
  }

  /**
   * Creates a binding for whether the file property is a valid java file, relative to the parent directory property.
   *
   * @param parentDirProperty the parent directory property
   * @param fileProperty      the file property
   * @return the created binding, telling us whether the file is a valid java file
   */
  public static BooleanBinding creatRelativeIsJavaFileBinding(ObjectProperty<Path> parentDirProperty, ObjectProperty<Path> fileProperty) {
    return Bindings.createBooleanBinding(
        () -> {
          if (parentDirProperty.isNull().get()) return false;
          if (fileProperty.isNull().get()) return false;
          return org.matwoess.jsourceprofiler.common.Util.isJavaFile(parentDirProperty.get().resolve(fileProperty.get()));
        },
        fileProperty,
        parentDirProperty
    );
  }

  /**
   * Creates a binding for whether the directory property is a valid directory,
   * relative to the parent directory property.
   *
   * @param dirProperty the directory property
   * @return the created binding, telling us whether the directory is a valid directory
   */
  public static BooleanBinding createIsDirectoryBinding(ObjectProperty<Path> dirProperty) {
    return Bindings.createBooleanBinding(
        () -> {
          if (dirProperty.isNull().get()) return false;
          return dirProperty.get().toFile().isDirectory();
        },
        dirProperty
    );
  }

  /**
   * Like {@link #createIsDirectoryBinding(ObjectProperty)}, but relative to the parent directory property.
   *
   * @param parentDirProperty the parent directory property
   * @param dirProperty       the directory property
   * @return the created binding, telling us whether the directory is a valid directory
   */
  public static BooleanBinding createRelativeIsDirectoryBinding(ObjectProperty<Path> parentDirProperty, ObjectProperty<Path> dirProperty) {
    return Bindings.createBooleanBinding(
        () -> {
          if (parentDirProperty.isNull().get()) return false;
          if (dirProperty.isNull().get()) return false;
          return parentDirProperty.get().resolve(dirProperty.get()).toFile().isDirectory();
        },
        dirProperty,
        parentDirProperty
    );
  }

  /**
   * Creates a binding for whether the file path property exists, relative to the parent directory property.
   *
   * @param parentDirProperty the parent directory property
   * @param relFilePath       the relative file path
   * @return the created binding, telling us whether the file exists
   */
  public static BooleanBinding creatRelativeFileExistsBinding(ObjectProperty<Path> parentDirProperty, Path relFilePath) {
    return Bindings.createBooleanBinding(
        () -> {
          if (parentDirProperty.isNull().get()) return false;
          return parentDirProperty.get().resolve(relFilePath).toFile().exists();
        },
        parentDirProperty
    );
  }

  /**
   * Create the border binding for a <code>SelectableTreeCell</code>.
   * <p>
   * Returns a brown border for the output directory,
   * a blue border for the sources directory and a green border for the main file.
   *
   * @param isOutDir       whether the item is the output directory
   * @param isSelectedDir  whether the item is the selected sources directory
   * @param isSelectedMain whether the item is the selected main file
   * @return the border binding depending on the input parameters
   */
  public static ObjectBinding<Border> createSelectedTreeItemBorderBinding(BooleanBinding isOutDir, BooleanBinding isSelectedDir, BooleanBinding isSelectedMain) {
    return Bindings.createObjectBinding(
        () -> {
          if (isOutDir.get()) return outDirBorder;
          if (isSelectedDir.get()) return srcDirBorder;
          if (isSelectedMain.get()) return mainFileBorder;
          else return neutralBorder;
        },
        isOutDir,
        isSelectedDir,
        isSelectedMain
    );
  }
}
