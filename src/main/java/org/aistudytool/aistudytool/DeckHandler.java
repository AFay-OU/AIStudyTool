package org.aistudytool.aistudytool;

import java.util.ArrayList;
import java.util.List;

public class DeckHandler {

    private static List<Deck> decks = new ArrayList<>();
    private static Deck activeDeck = null;

    public static void addDeck(Deck deck) {
        decks.add(deck);
        if (activeDeck == null) activeDeck = deck;
    }

    public static List<Deck> getDecks() {
        return decks;
    }

    public static Deck getActiveDeck() {
        return activeDeck;
    }

    public static void setActiveDeck(Deck deck) {
        activeDeck = deck;
    }
}
