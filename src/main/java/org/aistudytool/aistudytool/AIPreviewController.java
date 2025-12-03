package org.aistudytool.aistudytool;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class AIPreviewController {

    @FXML public Button addButton;
    @FXML public Button retryButton;
    @FXML public Button cancelButton;
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

    public void setOnAddCallback(Runnable r) { this.onAddCallback = r; }
    public void setOnRetryCallback(Runnable r) { this.onRetryCallback = r; }

    @FXML
    private void onAdd() {
        if (flashcard != null) {
            flashcard.setQuestion(questionArea.getText());
            flashcard.setAnswer(answerArea.getText());
        }

        chooseDeckForFlashcard();
    }

    @FXML
    private void onRetry() {
        if (onRetryCallback != null) onRetryCallback.run();
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

    public TextArea getQuestionArea() {
        return questionArea;
    }

    public TextArea getAnswerArea() {
        return answerArea;
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

                if (onAddCallback != null) onAddCallback.run(); // <--- notify main controller
            }

            closeWindow();
        });
    }

}
