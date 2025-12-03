package org.aistudytool.aistudytool;

import java.util.ArrayList;
import java.util.List;

public class DeckHandler {

    private static final List<FlashcardController> decks = new ArrayList<>();
    private static FlashcardController activeDeck = null;

    public static void addDeck(FlashcardController deck) {
        decks.add(deck);
        if (activeDeck == null) activeDeck = deck;
    }

    public static List<FlashcardController> getDecks() {
        return decks;
    }

    public static FlashcardController getActiveDeck() {
        return activeDeck;
    }

    public static void setActiveDeck(FlashcardController deck) {
        activeDeck = deck;
    }

    public static List<String> getDeckNames() {
        List<String> names = new ArrayList<>();
        for (FlashcardController d : decks) {
            names.add(d.getName());
        }
        return names;
    }

    public static FlashcardController getDeckByName(String name) {
        for (FlashcardController d : decks) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        return null;
    }
}
