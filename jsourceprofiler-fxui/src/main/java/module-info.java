module org.matwoess.jsourceprofiler.fxui {
  requires javafx.controls;
  requires javafx.fxml;
  requires atlantafx.base;
  requires java.desktop;
  requires org.matwoess.jsourceprofiler.common;
  requires org.matwoess.jsourceprofiler.tool;


  opens org.matwoess.jsourceprofiler.fxui to javafx.fxml;
  exports org.matwoess.jsourceprofiler.fxui;
  exports org.matwoess.jsourceprofiler.fxui.tree;
  exports org.matwoess.jsourceprofiler.fxui.model;
  opens org.matwoess.jsourceprofiler.fxui.tree to javafx.fxml;
}