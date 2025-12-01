package org.aistudytool.aistudytool;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FlashcardGUIController {

    @FXML private VBox flashcardPanel;
    @FXML private Label questionLabel;
    @FXML private Label answerLabel;
    @FXML private Button correctButton;
    @FXML private Button wrongButton;
    @FXML private VBox deckListPanel;
    @FXML private ListView<String> deckSummaryList;
    @FXML private Label topTitleLabel;
    @FXML private ListView<String> flashcardList;
    @FXML private Button showAnswerButton;
    @FXML private VBox aiPreviewBox;
    @FXML private Label aiQuestionLabel;
    @FXML private Label aiAnswerLabel;
    @FXML private Button addToDeckButton;
    @FXML private Button retryButton;
    @FXML private Button editButton;
    @FXML private Button cancelPreviewButton;
    @FXML private StackPane centerStack;

    private Flashcard currentCard;
    private Flashcard aiGeneratedCard;

    private final LlamaLLM llm = new LlamaLLM();

    @FXML
    public void initialize() {
        FlashcardStorageHandler.autoInit();
        refreshList();
        refreshDeckSummaryList();

        topTitleLabel.setText("AI Study Tool");

        deckListPanel.setVisible(true);
        deckListPanel.setManaged(true);
        flashcardPanel.setVisible(false);
        flashcardPanel.setManaged(false);

    }

    private void refreshDeckSummaryList() {
        if (deckSummaryList == null) return;

        deckSummaryList.getItems().clear();

        for (Deck d : DeckHandler.getDecks()) {
            deckSummaryList.getItems().add(
                    d.getName()
                            + " — " + d.getDueCards() + " due"
                            + " | " + d.getTotalCards() + " total"
                            + " | " + d.getNewCards() + " new"
            );
        }
    }

    private void autoSaveDecks() {
        try {
            FlashcardStorageHandler.saveAllDecks("flashcards.json");
        } catch (IOException e) {
            showError("Auto-save failed: " + e.getMessage());
        }
    }

    @FXML
    public void onDeckSelected() {
        int index = deckSummaryList.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        Deck selected = DeckHandler.getDecks().get(index);
        DeckHandler.setActiveDeck(selected);

        if (selected.getTotalCards() == 0) {
            showInfo("This deck has no cards yet.");
            return;
        }

        List<Flashcard> due = selected.getCards().stream()
                .filter(c -> c.getNextReview() <= System.currentTimeMillis())
                .toList();

        if (due.isEmpty()) {
            showInfo("No cards are due in this deck.");
            return;
        }

        updateTopLabelStats(selected);

        deckListPanel.setVisible(false);
        deckListPanel.setManaged(false);

        flashcardPanel.setVisible(true);
        flashcardPanel.setManaged(true);

        switchHomeToShowAnswerMode();
        displayCard(due.get(0));
    }

    @FXML
    public void onHome() {
        showDeckListView();
    }

    private void showDeckListView() {
        topTitleLabel.setText("AI Study Tool");

        deckListPanel.setVisible(true);
        deckListPanel.setManaged(true);

        flashcardPanel.setVisible(false);
        flashcardPanel.setManaged(false);

        refreshDeckSummaryList();
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
        if (currentCard == null) {
            showError("No card loaded.");
            return;
        }

        answerLabel.setVisible(true);
        correctButton.setDisable(false);
        wrongButton.setDisable(false);
    }


    @FXML
    public void onCorrect() {
        if (inFullReviewMode) {
            reviewAllIndex++;
            showCardInFullReview();
            return;
        }

        // Normal study mode
        Study.markCorrect(currentCard);
        updateTopLabelStats(DeckHandler.getActiveDeck());
        refreshDeckSummaryList();
        autoSaveDecks();
        onReview();
    }


    @FXML
    public void onWrong() {
        if (inFullReviewMode) {
            reviewAllIndex = Math.max(0, reviewAllIndex - 1);
            showCardInFullReview();
            return;
        }

        // Normal study mode
        Study.markIncorrect(currentCard);
        updateTopLabelStats(DeckHandler.getActiveDeck());
        refreshDeckSummaryList();
        autoSaveDecks();
        onReview();
    }


    @FXML
    public void onReview() {
        Deck active = DeckHandler.getActiveDeck();
        if (active == null) return;

        List<Flashcard> due = active.getCards().stream()
                .filter(c -> c.getNextReview() <= System.currentTimeMillis())
                .toList();

        if (due.isEmpty()) {
            questionLabel.setText("No cards are due today.");
            answerLabel.setVisible(false);

            switchShowAnswerToHomeMode();
            return;
        }

        switchHomeToShowAnswerMode();
        displayCard(due.get(0));
    }

    @FXML
    public void onReviewAllCards() {
        List<Deck> decks = DeckHandler.getDecks();
        if (decks.isEmpty()) {
            showError("No decks exist.");
            return;
        }

        // Let the user choose a deck
        ChoiceDialog<Deck> dialog = new ChoiceDialog<>(DeckHandler.getActiveDeck(), decks);
        dialog.setHeaderText("Choose a Deck to Review");
        dialog.setContentText("Select a deck:");

        Deck selectedDeck = dialog.showAndWait().orElse(null);
        if (selectedDeck == null) return;

        if (selectedDeck.getTotalCards() == 0) {
            showInfo("This deck has no flashcards to review.");
            return;
        }

        // Switch to review mode
        startFullReviewMode(selectedDeck);
    }

    private List<Flashcard> reviewAllList;
    private int reviewAllIndex = 0;
    private boolean inFullReviewMode = false;

    private void startFullReviewMode(Deck deck) {
        reviewAllList = deck.getCards();
        reviewAllIndex = 0;
        inFullReviewMode = true;

        deckListPanel.setVisible(false);
        deckListPanel.setManaged(false);

        flashcardPanel.setVisible(true);
        flashcardPanel.setManaged(true);

        showCardInFullReview();
    }

    private void showCardInFullReview() {
        if (reviewAllList.isEmpty()) {
            showInfo("This deck has no cards.");
            endFullReviewMode();
            return;
        }

        if (reviewAllIndex >= reviewAllList.size()) {
            showInfo("Finished reviewing all cards.");
            endFullReviewMode();
            return;
        }

        Flashcard card = reviewAllList.get(reviewAllIndex);
        currentCard = card;

        questionLabel.setText(card.getQuestion());
        answerLabel.setText(card.getAnswer());
        answerLabel.setVisible(false);

        // Disable spaced repetition buttons but show them
        correctButton.setText("Next");
        wrongButton.setText("Back");

        correctButton.setDisable(false);
        wrongButton.setDisable(false);

        showAnswerButton.setText("Show Answer");
        showAnswerButton.setOnAction(e -> onShowAnswer());
    }

    private void endFullReviewMode() {
        inFullReviewMode = false;

        // Reset button text
        correctButton.setText("Correct");
        wrongButton.setText("Wrong");

        showDeckListView();
    }



    private void switchShowAnswerToHomeMode() {
        showAnswerButton.setText("Home");
        showAnswerButton.setOnAction(e -> onHome());
        correctButton.setDisable(true);
        wrongButton.setDisable(true);
    }

    private void switchHomeToShowAnswerMode() {
        showAnswerButton.setText("Show Answer");
        showAnswerButton.setOnAction(e -> onShowAnswer());
        correctButton.setDisable(true);
        wrongButton.setDisable(true);
    }

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

        Deck chosenDeck = chooseDeckForNewCard();
        if (chosenDeck == null) return;

        chosenDeck.addCard(card);
        DeckHandler.setActiveDeck(chosenDeck);

        refreshList();
        refreshDeckSummaryList();
        autoSaveDecks();
        updateTopLabelStats(chosenDeck);
        showDeckListView();
    }

    private Deck chooseDeckForNewCard() {
        List<Deck> decks = DeckHandler.getDecks();

        ChoiceDialog<Deck> dialog =
                new ChoiceDialog<>(DeckHandler.getActiveDeck(), decks);

        dialog.setHeaderText("Choose Deck");
        dialog.setContentText("Select which deck to save this flashcard to:");

        return dialog.showAndWait().orElse(null);
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

        refreshDeckSummaryList();
        updateTopLabelStats(deck);
        autoSaveDecks();
        refreshList();

        showInfo("Created new deck: " + name);
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
                refreshDeckSummaryList();
                refreshList();
                showInfo("Loaded all decks.");
            } catch (IOException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void onEditFlashcards() {
        List<Deck> decks = DeckHandler.getDecks();

        if (decks.isEmpty()) {
            showError("No decks exist.");
            return;
        }

        ChoiceDialog<Deck> dialog = new ChoiceDialog<>(DeckHandler.getActiveDeck(), decks);
        dialog.setHeaderText("Choose a Deck to Edit");
        dialog.setContentText("Select a deck:");

        Deck chosenDeck = dialog.showAndWait().orElse(null);
        if (chosenDeck == null) return;

        if (chosenDeck.getTotalCards() == 0) {
            showInfo("This deck has no flashcards to edit.");
            return;
        }

        openFlashcardEditor(chosenDeck);
    }


    private void openFlashcardEditor(Deck deck) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FlashcardEditor.fxml"));
            Parent root = loader.load();

            FlashcardEditorController controller = loader.getController();
            controller.setDeck(deck);

            Runnable refresh = () -> {
                refreshDeckSummaryList();
                refreshList();

                // Return UI to Home mode
                topTitleLabel.setText("AI Study Tool");
                deckListPanel.setVisible(true);
                deckListPanel.setManaged(true);

                flashcardPanel.setVisible(false);
                flashcardPanel.setManaged(false);
            };


            controller.setOnCloseCallback(refresh);

            Stage popup = new Stage();
            popup.setTitle("Edit Flashcards — " + deck.getName());
            popup.setScene(new Scene(root));

            popup.setOnHidden(e -> refresh.run());

            popup.show();

        } catch (Exception e) {
            showError("Failed to open editor: " + e.getMessage());
            e.printStackTrace();
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
            AppState.getRedoState().clear();

            ScreenshotHandler sh = new ScreenshotHandler();
            sh.setOnOCRComplete(text -> Platform.runLater(() -> showOCRConfirmation(text)));
        } catch (Exception e) {
            showError("Screenshot failed: " + e.getMessage());
        }
    }


    private void startPDFUploadMode() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File selected = chooser.showOpenDialog(null);
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PDFViewPane.fxml"));
            Parent root = loader.load();

            PDFViewController controller = loader.getController();
            controller.loadPDF(selected);

            controller.setOnConfirm(text -> Platform.runLater(() -> showOCRConfirmation(text)));
            controller.setOnCancel(() -> System.out.println("PDF view canceled."));

            Stage popup = new Stage();
            popup.setTitle("PDF Viewer");
            popup.setScene(new Scene(root));
            popup.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open PDF viewer.");
        }
    }

    private void showOCRConfirmation(String rawText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ExtractedTextDialog.fxml"));
            Parent root = loader.load();

            ExtractedTextDialogController controller = loader.getController();
            controller.setInitialText(rawText);

            controller.setOnConfirm(finalText -> createFlashcardsFromExtractedText(finalText));
            controller.setOnRetry(() -> Platform.runLater(this::retryLastExtraction));

            Stage stage = new Stage();
            stage.setTitle("Confirm Extracted Text");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showError("Failed to open confirmation dialog: " + e.getMessage());
        }
    }

    private void retryLastExtraction() {
        RedoState redo = AppState.getRedoState();
        if (redo.hasPDF()) {
            openPDFWithRestore(
                    redo.getPdfFile(),
                    redo.getPdfPage(),
                    redo.getNormX(),
                    redo.getNormY(),
                    redo.getNormW(),
                    redo.getNormH()
            );
            return;
        }
        startScreenshotMode();
    }

    private void redoScreenshot() {
        startScreenshotMode();
    }

    @FXML
    private void onRedo() {
        RedoState redo = AppState.getRedoState();

        System.out.println("Redo PDF: " + redo.hasPDF());
        System.out.println("Redo Screenshot: " + redo.hasScreenshot());

        if (redo.hasPDF()) {
            openPDFWithRestore(
                    redo.getPdfFile(),
                    redo.getPdfPage(),
                    redo.getNormX(),
                    redo.getNormY(),
                    redo.getNormW(),
                    redo.getNormH()
            );
            return;
        }

        if (redo.hasScreenshot()) {
            redoScreenshot();
            return;
        }

        showError("Nothing to redo.");
    }


    private void openPDFWithRestore(File file, int page,
                                    double nx, double ny,
                                    double nw, double nh) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PDFViewPane.fxml"));
            Parent root = loader.load();

            PDFViewController controller = loader.getController();

            controller.loadPDF(file);
            controller.setOnConfirm(text -> Platform.runLater(() -> showOCRConfirmation(text)));

            controller.displayPage(page); // Go to correct page

            controller.restoreSelection(nx, ny, nw, nh);

            Stage popup = new Stage();
            popup.setTitle("PDF Viewer");
            popup.setScene(new Scene(root));
            popup.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Redo failed: " + e.getMessage());
        }
    }

    private void createFlashcardsFromExtractedText(String confirmedText) {
        try {
            // Generate flashcard using LLM
            aiGeneratedCard = llm.generateFlashcard(confirmedText);

            // Fill preview UI
            aiQuestionLabel.setText(aiGeneratedCard.getQuestion());
            aiAnswerLabel.setText(aiGeneratedCard.getAnswer());

            showAIPreviewPopup();


        } catch (Exception e) {
            showError("AI Flashcard generation failed:\n" + e.getMessage());
        }
    }

    private void showAIPreviewPopup() {
        try {
            var url = getClass().getResource("AIPreview.fxml");

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AIPreviewController controller = loader.getController();
            controller.setFlashcard(aiGeneratedCard);

            controller.setOnAddCallback(() -> {
                refreshList();
                refreshDeckSummaryList();
                updateTopLabelStats(DeckHandler.getActiveDeck());
                autoSaveDecks();
                topTitleLabel.setText("AI Study Tool");
                showDeckListView();
            });

            controller.setOnRetryCallback(() -> {
                try {
                    Flashcard card = controller.getFlashcard();
                    String src = card.getQuestion() + "\n" + card.getAnswer();
                    aiGeneratedCard = llm.generateFlashcard(src);
                    controller.setFlashcard(aiGeneratedCard);
                } catch (Exception ex) {
                    showError("Retry failed: " + ex.getMessage());
                }
            });

            Stage popup = new Stage();
            popup.setTitle("AI Flashcard Preview");
            popup.setScene(new Scene(root));
            popup.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open AI preview window: " + e.getMessage());
        }
    }




    private void updateTopLabelStats(Deck deck) {
        if (deck == null) return;

        int due = deck.getDueCards();
        int total = deck.getTotalCards();
        int newCards = deck.getNewCards();

        topTitleLabel.setText(
                deck.getName()
                        + " — " + due + " due"
                        + " | " + total + " total"
                        + " | " + newCards + " new"
        );
    }

    // Helpers
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }

    @FXML
    private void onAddToDeckButton() {
        if (aiGeneratedCard == null) return;

        List<String> decks = DeckHandler.getDeckNames();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(DeckHandler.getActiveDeck().getName(), decks);
        dialog.setHeaderText("Choose Deck");
        dialog.setContentText("Add flashcard to:");

        dialog.showAndWait().ifPresent(deckName -> {
            Deck deck = DeckHandler.getDeckByName(deckName);
            assert deck != null;
            deck.addCard(aiGeneratedCard);

            showInfo("Added to " + deckName);
            aiPreviewBox.setVisible(false);
            aiPreviewBox.setManaged(false);

            refreshList();
            refreshDeckSummaryList();
            updateTopLabelStats(deck);
            autoSaveDecks();
        });
    }

    @FXML
    private void onRetryAICard() {
        if (aiGeneratedCard == null) return;

        try {
            aiGeneratedCard = llm.generateFlashcard(
                    aiQuestionLabel.getText() + "\n" + aiAnswerLabel.getText()
            );

            aiQuestionLabel.setText(aiGeneratedCard.getQuestion());
            aiAnswerLabel.setText(aiGeneratedCard.getAnswer());

        } catch (Exception e) {
            showError("Retry failed: " + e.getMessage());
        }
    }

    @FXML
    private void onEditAICard() {
        if (aiGeneratedCard == null) return;

        TextInputDialog qDialog = new TextInputDialog(aiGeneratedCard.getQuestion());
        qDialog.setHeaderText("Edit Question");
        String newQ = qDialog.showAndWait().orElse(aiGeneratedCard.getQuestion());

        TextInputDialog aDialog = new TextInputDialog(aiGeneratedCard.getAnswer());
        aDialog.setHeaderText("Edit Answer");
        String newA = aDialog.showAndWait().orElse(aiGeneratedCard.getAnswer());

        aiGeneratedCard.setQuestion(newQ);
        aiGeneratedCard.setAnswer(newA);

        aiQuestionLabel.setText(newQ);
        aiAnswerLabel.setText(newA);
    }

    @FXML
    private void onCancelAIPreview() {
        aiPreviewBox.setVisible(false);
        aiPreviewBox.setManaged(false);
        aiGeneratedCard = null;
    }
}
