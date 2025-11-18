package org.aistudytool.aistudytool;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ExtractedTextDialogController {

    @FXML private TextArea textArea;

    private Consumer<String> onConfirmCallback;
    private Runnable onRetryCallback;   // NEW

    public void setInitialText(String text) {
        textArea.setText(text);
    }

    public void setOnConfirm(Consumer<String> callback) {
        this.onConfirmCallback = callback;
    }

    // NEW
    public void setOnRetry(Runnable callback) {
        this.onRetryCallback = callback;
    }

    @FXML
    public void onCancel() {
        ((Stage) textArea.getScene().getWindow()).close();
    }

    @FXML
    public void onRetry() {
        ((Stage) textArea.getScene().getWindow()).close();
        if (onRetryCallback != null) {
            onRetryCallback.run();
        }
    }

    @FXML
    public void onConfirm() {
        if (onConfirmCallback != null) {
            onConfirmCallback.accept(textArea.getText());
        }
        ((Stage) textArea.getScene().getWindow()).close();
    }
}
