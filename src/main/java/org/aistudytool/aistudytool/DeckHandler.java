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

    public static List<String> getDeckNames() {
        List<String> names = new ArrayList<>();
        for (Deck d : decks) {
            names.add(d.getName());
        }
        return names;
    }

    public static Deck getDeckByName(String name) {
        for (Deck d : decks) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        return null;
    }

    public static int countDueCards(Deck deck) {
        if (deck == null) return 0;

        long now = System.currentTimeMillis();

        return (int) deck.getCards().stream()
                .filter(c -> c.getNextReview() <= now)
                .count();
    }
}
