package org.aistudytool.aistudytool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FlashcardController {

    private final List<Flashcard> cards = new ArrayList<>();
    private String name = "Untitled Deck";

    public List<Flashcard> getFlashcards() {
        return cards;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCard(Flashcard card){
        cards.add(card);
    }

    public List<Flashcard> dueCards() {
        long now = System.currentTimeMillis();
        return cards.stream()
                .filter(c -> c.getNextReview() <= now)
                .collect(Collectors.toList());
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
