package org.aistudytool.aistudytool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;

public class StudyTest {

    Flashcard card;

    @BeforeEach
    public void setup() {
        card = new Flashcard("Q1", "A2");
        card.setSeen();
        card.setNextReview(0);
        card.setBox(1);
    }

    @Test
    public void studyTest1() {
        // Test that a correct answer moves card to next box
        int oldBox = card.getBox();

        Study.markCorrect(card);

        assertEquals(oldBox + 1, card.getBox(),
                "Card should move to the next box when correct");
    }

    @Test
    public void studyTest2() {
        // Test that a correct answer updates review time
        Study.markCorrect(card);

        long expected = LocalDate.now()
                .plusDays(StudyIntervalsForBox(card.getBox()))
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000;

        assertEquals(expected, card.getNextReview(),
                "nextReview should be updated correctly");
    }

    @Test
    public void studyTest3() {
        // Test for incorrect answer lowers box number
        card.setBox(3);

        Study.markIncorrect(card);

        assertEquals(1, card.getBox(),
                "Incorrect answer should reset box to 1");
    }

    @Test
    public void studyTest4() {
        // Test for incorrect answer updating review time
        Study.markIncorrect(card);

        long expected = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000;

        assertEquals(expected, card.getNextReview(),
                "nextReview must be updated to 1 day ahead");
    }

    @Test
    public void studyTest5() {
        // Test correct at the max box
        card.setBox(5);

        Study.markCorrect(card);

        assertEquals(5, card.getBox(),
                "Box 5 should remain at 5 when marked correct");
    }

    @Test
    public void studyTest6() {
        // Test due cards
        FlashcardController controller = new FlashcardController();
        controller.addCard(card);

        Flashcard notDue = new Flashcard("What year is it currently?", "2025");
        long tomorrow = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000;

        notDue.setNextReview(tomorrow);
        controller.addCard(notDue);

        var due = controller.dueCards();

        assertTrue(due.contains(card), "Due card should appear in list");
        assertFalse(due.contains(notDue), "Not-due card should not appear");
    }

    private int StudyIntervalsForBox(int box) {
        int[] intervals = {0, 1, 2, 5, 10, 30};
        return intervals[box];
    }
}
