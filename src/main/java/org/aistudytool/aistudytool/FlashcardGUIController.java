package org.aistudytool.aistudytool;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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
        if (flashcardList != null){
            flashcardList.getItems().clear();
            for (Flashcard c : deck.getFlashcards()) {
                flashcardList.getItems().add("[" + c.getCategory() + "] " + c.getQuestion());
            }
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

        String category = (categoryCombo.getValue() != null)
                ? categoryCombo.getValue()
                : "General";

        Flashcard card = new Flashcard(q, a);
        card.setCategory(category);

        deck.addCard(card);
        refreshList();
    }


    @FXML
    public void onSaveDeck(){
        FileChooser fc = new FileChooser();
        fc.setTitle("deck.json");
        File file = fc.showSaveDialog(null);

        if (file != null){
            try {
                FlashcardStorageHandler.saveFC(deck, file.getAbsolutePath());
            } catch (IOException e){
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void onLoadDeck() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);

        if (file != null) {
            try {
                deck = FlashcardStorageHandler.loadFC(file.getAbsolutePath());
                refreshList();
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
        } catch (Exception e) {
            showError("Screenshot failed: " + e.getMessage());
        }
    }

    private void startPDFUploadMode() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showOpenDialog(null);

        if (file == null) return;

        // TODO: integrate your PDFHandler OCR
        showInfo("PDF selected: " + file.getName() + "\n(Implement text extraction next)");
    }


    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}
