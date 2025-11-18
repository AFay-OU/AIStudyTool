package org.aistudytool.aistudytool;

import java.util.ArrayList;
import java.util.List;

public class Deck {

    private String name;
    private List<Flashcard> cards = new ArrayList<>();

    public Deck(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public List<Flashcard> getCards() { return cards; }

    public void addCard(Flashcard card) {
        cards.add(card);
    }

    @Override
    public String toString() {
        return name;
    }
}
