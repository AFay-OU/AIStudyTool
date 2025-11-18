package org.aistudytool.aistudytool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class FlashcardStorageHandler {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveAllDecks(String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            gson.toJson(DeckHandler.getDecks(), fw);
        }
    }

    public static void loadAllDecks(String filename) throws IOException {
        try (FileReader fr = new FileReader(filename)) {
            Deck[] loaded = gson.fromJson(fr, Deck[].class);
            DeckHandler.getDecks().clear();
            for (Deck d : loaded) {
                DeckHandler.addDeck(d);
            }
            if (DeckHandler.getDecks().size() > 0) {
                DeckHandler.setActiveDeck(DeckHandler.getDecks().get(0));
            }
        }
    }
}
