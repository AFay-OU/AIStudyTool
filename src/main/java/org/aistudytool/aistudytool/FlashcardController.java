package org.aistudytool.aistudytool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FlashcardController {

    public List<Flashcard> cards = new ArrayList<>();
    public String name = "Untitled Deck";

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
        return cards.stream().filter(c -> c.getNextReview() <= now).collect(Collectors.toList());
    }
}
