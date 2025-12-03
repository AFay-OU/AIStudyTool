package org.aistudytool.aistudytool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FlashcardControllerTest {

    private FlashcardController deck;

    @BeforeEach
    public void setup() {
        deck = new FlashcardController();
    }

    @Test
    public void fccTest1() {
        // Test addCard()
        Flashcard card = new Flashcard("Q1", "A1");
        deck.addCard(card);

        assertEquals(1, deck.getFlashcards().size(),
                "Deck should contain 1 flashcard after adding.");
        assertEquals("Q1", deck.getFlashcards().getFirst().getQuestion());
    }

    @Test
    public void fccTest2() {
        // Test for returned flashcards
        Flashcard c1 = new Flashcard("Q1", "A1");
        Flashcard c2 = new Flashcard("Q2", "A2");

        deck.addCard(c1);
        deck.addCard(c2);

        List<Flashcard> cards = deck.getFlashcards();

        assertEquals(2, cards.size(), "Deck should return 2 flashcards.");
        assertTrue(cards.contains(c1));
        assertTrue(cards.contains(c2));
    }

    @Test
    public void fccTest3() {
        // Test for non-due flashcards
        Flashcard c1 = new Flashcard("Q1", "A1");
        Flashcard c2 = new Flashcard("Q2", "A2");

        long future = System.currentTimeMillis() + 100000;
        c1.setNextReview(future);
        c2.setNextReview(future);

        deck.addCard(c1);
        deck.addCard(c2);

        List<Flashcard> due = deck.dueCards();
        assertTrue(due.isEmpty(), "No cards should be due.");
    }

    @Test
    public void fccTest4() {
        // Test for partially due cards
        Flashcard dueCard = new Flashcard("Due?", "Yes");
        Flashcard notDue = new Flashcard("Not due?", "No");

        long now = System.currentTimeMillis();
        long future = now + 100000;

        dueCard.setNextReview(now - 1000);
        notDue.setNextReview(future);

        deck.addCard(dueCard);
        deck.addCard(notDue);

        List<Flashcard> due = deck.dueCards();

        assertEquals(1, due.size(), "Only one card should be due.");
        assertEquals("Due?", due.getFirst().getQuestion());
    }

    @Test
    public void fccTest5() {
        // Test for every card being due
        Flashcard c1 = new Flashcard("Q1", "A1");
        Flashcard c2 = new Flashcard("Q2", "A2");

        long now = System.currentTimeMillis();
        c1.setNextReview(now - 1000);
        c2.setNextReview(now - 2000);

        deck.addCard(c1);
        deck.addCard(c2);

        List<Flashcard> due = deck.dueCards();

        assertEquals(2, due.size(), "Both cards should be due.");
        assertTrue(due.contains(c1));
        assertTrue(due.contains(c2));
    }

    @Test
    public void fccTest6() {
        // Test for empty deck
        List<Flashcard> due = deck.dueCards();
        assertNotNull(due, "Due list should not be null.");
        assertTrue(due.isEmpty(), "Due list should be empty when deck has no cards.");
    }
}
