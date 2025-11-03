module org.aistudytool.aistudytool {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.aistudytool.aistudytool to javafx.fxml;
    exports org.aistudytool.aistudytool;
}