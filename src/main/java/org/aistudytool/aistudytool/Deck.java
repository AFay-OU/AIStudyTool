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

    public int getTotalCards() {
        return cards.size();
    }

    public int getDueCards() {
        long now = System.currentTimeMillis();
        return (int) cards.stream()
                .filter(c -> c.getNextReview() <= now)
                .count();
    }

    public int getNewCards() {
        return (int) cards.stream()
                .filter(c -> c.getBox() == 1)
                .count();
    }

    @Override
    public String toString() {
        return name;
    }
}
