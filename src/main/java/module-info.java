module org.aistudytool.aistudytool {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.logging;
    requires tess4j;
    requires com.google.gson;

    opens org.aistudytool.aistudytool to javafx.fxml, com.google.gson;
    exports org.aistudytool.aistudytool;
}