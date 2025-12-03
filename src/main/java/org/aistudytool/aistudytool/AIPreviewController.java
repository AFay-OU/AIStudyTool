package org.aistudytool.aistudytool;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class AIPreviewController {

    @FXML private TextArea questionArea;
    @FXML private TextArea answerArea;

    private Flashcard flashcard;
    private Runnable onAddCallback;
    private Runnable onRetryCallback;

    public void setFlashcard(Flashcard card) {
        this.flashcard = card;
        questionArea.setText(card.getQuestion());
        answerArea.setText(card.getAnswer());
    }

    public void setOnAddCallback(Runnable callback) {
        this.onAddCallback = callback;
    }

    public void setOnRetryCallback(Runnable callback) {
        this.onRetryCallback = callback;
    }

    @FXML
    private void onAdd() {
        chooseDeckForFlashcard();
    }

    @FXML
    private void onRetry() {
        if (onRetryCallback != null) onRetryCallback.run();
    }

    @FXML
    private void onEdit() {
        flashcard.setQuestion(prompt("Edit Question:", flashcard.getQuestion()));
        flashcard.setAnswer(prompt("Edit Answer:", flashcard.getAnswer()));

        questionArea.setText(flashcard.getQuestion());
        answerArea.setText(flashcard.getAnswer());
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) questionArea.getScene().getWindow();
        stage.close();
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    private String prompt(String title, String defaultValue) {
        var dialog = new javafx.scene.control.TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        return dialog.showAndWait().orElse(defaultValue);
    }

    private void chooseDeckForFlashcard() {
        var deckNames = DeckHandler.getDecks()
                .stream()
                .map(FlashcardController::getName)
                .toList();

        if (deckNames.isEmpty()) {
            System.out.println("No decks available.");
            closeWindow();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(deckNames.getFirst(), deckNames);
        dialog.setTitle("Choose Deck");
        dialog.setHeaderText("Add flashcard to which deck?");
        dialog.setContentText("Deck:");

        dialog.showAndWait().ifPresent(selectedName -> {

            FlashcardController selectedDeck = DeckHandler.getDecks()
                    .stream()
                    .filter(d -> d.getName().equals(selectedName))
                    .findFirst()
                    .orElse(null);

            if (selectedDeck != null) {
                selectedDeck.addCard(flashcard);
                DeckHandler.setActiveDeck(selectedDeck);
            }

            if (onAddCallback != null) onAddCallback.run();
            closeWindow();
        });
    }
}
