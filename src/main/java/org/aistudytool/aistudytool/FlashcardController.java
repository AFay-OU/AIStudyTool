package org.aistudytool.aistudytool;

import java.util.ArrayList;
import java.util.List;

public class FlashcardController {

    private List<Flashcard> cards = new ArrayList<>();
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
}
