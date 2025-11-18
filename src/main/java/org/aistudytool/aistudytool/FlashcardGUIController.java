package org.aistudytool.aistudytool;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FlashcardGUIController {

    private FlashcardController deck = new FlashcardController();

    @FXML private ComboBox<String> inputModeCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private VBox flashcardPanel;
    @FXML private Label questionLabel;
    @FXML private Label answerLabel;
    @FXML private Button correctButton;
    @FXML private Button wrongButton;

    // On Load

    @FXML
    private ListView<String> flashcardList;

    @FXML
    public void initialize() {
        refreshList();
    }

    private void refreshList() {
        if (flashcardList == null) return;

        flashcardList.getItems().clear();

        Deck active = DeckHandler.getActiveDeck();
        if (active == null) return;

        for (Flashcard c : active.getCards()) {
            flashcardList.getItems().add("[" + c.getCategory() + "] " + c.getQuestion());
        }
    }


    private Flashcard currentCard;

    public void displayCard(Flashcard card) {
        currentCard = card;
        questionLabel.setText(card.getQuestion());
        answerLabel.setText(card.getAnswer());
        answerLabel.setVisible(false);

        correctButton.setDisable(true);
        wrongButton.setDisable(true);
    }

    @FXML
    public void onShowAnswer() {
        answerLabel.setVisible(true);
        correctButton.setDisable(false);
        wrongButton.setDisable(false);
    }

    @FXML
    public void onCorrect() {
        Study.markCorrect(currentCard);
        onReview();
    }

    @FXML
    public void onWrong() {
        Study.markIncorrect(currentCard);
        onReview();
    }

    // Menu
    @FXML
    public void onAddCard() {
        TextInputDialog qDialog = new TextInputDialog();
        qDialog.setHeaderText("New Flashcard");
        qDialog.setContentText("Enter Question:");
        String q = qDialog.showAndWait().orElse(null);
        if (q == null) return;

        TextInputDialog aDialog = new TextInputDialog();
        aDialog.setHeaderText("Answer");
        aDialog.setContentText("Enter answer:");
        String a = aDialog.showAndWait().orElse(null);
        if (a == null) return;

        Flashcard card = new Flashcard(q, a);

        // Ask user which deck to save to
        Deck chosenDeck = chooseDeckForNewCard();
        if (chosenDeck == null) return;

        chosenDeck.addCard(card);

        DeckHandler.setActiveDeck(chosenDeck);
        refreshList();
    }



    @FXML
    public void onSaveAllDecks() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save All Decks");
        File file = fc.showSaveDialog(null);

        if (file != null) {
            try {
                FlashcardStorageHandler.saveAllDecks(file.getAbsolutePath());
                showInfo("Saved all decks.");
            } catch (IOException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void onLoadAllDecks() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);

        if (file != null) {
            try {
                FlashcardStorageHandler.loadAllDecks(file.getAbsolutePath());
                refreshList();
                showInfo("Loaded all decks.");
            } catch (IOException e) {
                showError(e.getMessage());
            }
        }
    }


    @FXML
    public void onReview() {
        List<Flashcard> due = deck.dueCards();

        if (due.isEmpty()) {
            questionLabel.setText("No cards are due today.");
            answerLabel.setVisible(false);
            return;
        }

        displayCard(due.get(0));
    }

    @FXML
    public void onStartInputMode() {
        String mode = inputModeCombo.getValue();

        if (mode == null) {
            showError("Please select Screenshot or PDF Upload.");
            return;
        }

        switch (mode) {
            case "Screenshot":
                startScreenshotMode();
                break;

            case "PDF Upload":
                startPDFUploadMode();
                break;
        }
    }

    @FXML
    public void onStartScreenshot() {
        startScreenshotMode();
    }

    @FXML
    public void onStartPDFUpload() {
        startPDFUploadMode();
    }

    private void startScreenshotMode() {
        try {
            ScreenshotHandler sh = new ScreenshotHandler();

            sh.setOnOCRComplete(text -> {
                // Run on JavaFX thread
                Platform.runLater(() -> showOCRConfirmation(text));
            });

        } catch (Exception e) {
            showError("Screenshot failed: " + e.getMessage());
        }
    }

    private void startPDFUploadMode() {
        // TODO: integrate PDFHandler
    }


    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }

    @FXML
    public void onNewDeck() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Create New Deck");
        dialog.setContentText("Enter deck name:");

        String name = dialog.showAndWait().orElse(null);
        if (name == null || name.trim().isEmpty()) return;

        Deck deck = new Deck(name.trim());
        DeckHandler.addDeck(deck);
        DeckHandler.setActiveDeck(deck);

        showInfo("Created new deck: " + name);

        refreshList();
    }

    private Deck chooseDeckForNewCard() {
        List<Deck> decks = DeckHandler.getDecks();

        ChoiceDialog<Deck> dialog =
                new ChoiceDialog<>(DeckHandler.getActiveDeck(), decks);

        dialog.setHeaderText("Choose Deck");
        dialog.setContentText("Select which deck to save this flashcard to:");

        return dialog.showAndWait().orElse(null);
    }

    private void showOCRConfirmation(String rawText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ExtractedTextDialog.fxml"));
            Parent root = loader.load();

            ExtractedTextDialogController controller = loader.getController();
            controller.setInitialText(rawText);

            controller.setOnConfirm(finalText -> {
                createFlashcardsFromExtractedText(finalText);
            });

            controller.setOnRetry(() -> {
                Platform.runLater(() -> startScreenshotMode());
            });

            Stage stage = new Stage();
            stage.setTitle("Confirm Extracted Text");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showError("Failed to open confirmation dialog: " + e.getMessage());
        }
    }

    private void createFlashcardsFromExtractedText(String text) {
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            Flashcard card = new Flashcard(line.trim(), ""); // Answer empty for now
            deck.addCard(card);
        }

        refreshList();
    }
}
