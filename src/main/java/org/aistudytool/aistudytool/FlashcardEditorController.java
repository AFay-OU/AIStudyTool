package org.aistudytool.aistudytool;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FlashcardEditorController {

    @FXML private ListView<String> flashcardListView;
    @FXML private TextArea questionArea;
    @FXML private TextArea answerArea;

    private FlashcardController deck;
    private Flashcard selectedCard;
    private Runnable onCloseCallback;


    public void setDeck(FlashcardController deck) {
        this.deck = deck;

        flashcardListView.getItems().clear();
        for (Flashcard card : deck.getFlashcards()) {
            flashcardListView.getItems().add(card.getQuestion());
        }

        flashcardListView.getSelectionModel().selectedIndexProperty().addListener((_, _, index) -> {
            if (index.intValue() < 0) return;
            selectedCard = deck.getFlashcards().get(index.intValue());
            questionArea.setText(selectedCard.getQuestion());
            answerArea.setText(selectedCard.getAnswer());
        });
    }

    @FXML
    private void onSave() {
        if (selectedCard == null) return;

        selectedCard.setQuestion(questionArea.getText());
        selectedCard.setAnswer(answerArea.getText());

        int i = deck.getFlashcards().indexOf(selectedCard);
        flashcardListView.getItems().set(i, selectedCard.getQuestion());

        FlashcardStorageHandler.autoSave();

        close();
    }

    @FXML
    private void onDelete() {
        if (selectedCard == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Flashcard");
        confirm.setHeaderText("Are you sure you want to delete this flashcard?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        int index = deck.getFlashcards().indexOf(selectedCard);

        deck.getFlashcards().remove(selectedCard);

        flashcardListView.getItems().remove(index);

        FlashcardStorageHandler.autoSave();

        questionArea.clear();
        answerArea.clear();
        selectedCard = null;

        if (deck.getFlashcards().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "No flashcards remain in this deck.");
            a.showAndWait();
            close();
        }
    }


    @FXML
    private void onCancel() {
        close();
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }


    private void close() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }

        Stage stage = (Stage) questionArea.getScene().getWindow();
        stage.close();
    }

}
