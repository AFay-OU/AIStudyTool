module org.aistudytool.aistudytool {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.logging;
    requires tess4j;
    requires com.google.gson;
    requires org.apache.pdfbox;
    requires ollama4j;

    opens org.aistudytool.aistudytool to javafx.fxml, com.google.gson, org.apache.pdfbox;
    exports org.aistudytool.aistudytool;
}
