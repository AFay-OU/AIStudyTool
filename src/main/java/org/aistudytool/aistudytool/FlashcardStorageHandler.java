package org.aistudytool.aistudytool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class FlashcardStorageHandler {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_FILE = "flashcards.json";

    public static void autoInit() {
        File f = new File(DEFAULT_FILE);
        if (f.exists()) {
            try {
                loadAllDecks(DEFAULT_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            DeckHandler.getDecks().clear();

            FlashcardController defaultDeck = new FlashcardController();
            defaultDeck.setName("Default Deck");

            DeckHandler.addDeck(defaultDeck);
            DeckHandler.setActiveDeck(defaultDeck);

            try {
                saveAllDecks(DEFAULT_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteDeck(String deckName) {
        List<FlashcardController> decks = DeckHandler.getDecks();

        FlashcardController toRemove = decks.stream()
                .filter(d -> d.getName().equals(deckName))
                .findFirst()
                .orElse(null);

        if (toRemove == null)
            return false;

        decks.remove(toRemove);

        if (DeckHandler.getActiveDeck() == toRemove) {
            if (!decks.isEmpty()) {
                DeckHandler.setActiveDeck(decks.getFirst());
            } else {
                FlashcardController fresh = new FlashcardController();
                fresh.setName("Default Deck");
                decks.add(fresh);
                DeckHandler.setActiveDeck(fresh);
            }
        }

        autoSave();
        return true;
    }

    public static void saveAllDecks(String filename) throws IOException {
        List<FlashcardController> decks = DeckHandler.getDecks();

        try (FileWriter fw = new FileWriter(filename)) {
            gson.toJson(decks, fw);
        }
    }

    public static void loadAllDecks(String filename) throws IOException {
        try (FileReader fr = new FileReader(filename)) {

            FlashcardController[] loaded = gson.fromJson(fr, FlashcardController[].class);

            DeckHandler.getDecks().clear();
            DeckHandler.getDecks().addAll(Arrays.asList(loaded));

            if (!DeckHandler.getDecks().isEmpty()) {
                DeckHandler.setActiveDeck(DeckHandler.getDecks().getFirst());
            }
        }
    }

    public static void autoSave() {
        try {
            saveAllDecks(DEFAULT_FILE);
        } catch (Exception ignored) {}
    }
}
