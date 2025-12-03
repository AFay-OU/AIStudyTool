package org.aistudytool.aistudytool;

import java.time.LocalDate;
import java.time.ZoneId;

public class Study {

    // index 0 = unused / box 1 = review daily / box 2 = review every 2 days
    // box 3 = 5 days / box 4 = 10 days / box 5 = 30 days
    private static final int[] intervals = {
      0, 1, 2, 5, 10, 30
    };

    public static void markCorrect(Flashcard card) {

        int box = card.getBox();

        if (box == 1 && !card.isSeen()) {
            card.setSeen();

            long tomorrowStart = java.time.LocalDate.now()
                    .plusDays(1)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toEpochSecond() * 1000;

            card.setNextReview(tomorrowStart);
            System.out.println("Next review: Tomorrow (start of day)");
            return;
        }

        if (box == 1 && card.isSeen()) {
            card.setBox(2);
            updateNextReview(card);
            System.out.println("Promoted to box 2");
            return;
        }

        card.setSeen();
        if (box < 5) {
            card.setBox(box + 1);
        }
        updateNextReview(card);
    }

    public static void markIncorrect(Flashcard card) {

        int box = card.getBox();

        if (box > 1) {
            card.setBox(box - 1);
        } else {
            card.setBox(1);
        }

        card.setNextReview(System.currentTimeMillis());
        System.out.println("Next review: Due immediately");
    }



    public static String getTimeUntilReview(Flashcard card) {
        long now = System.currentTimeMillis();
        long next = card.getNextReview();

        long diff = next - now;
        if (diff <= 0) return "Due now";

        long hours = diff / (1000 * 60 * 60);
        long minutes = (diff / (1000 * 60)) % 60;

        return hours + "h " + minutes + "m";
    }

    private  static void updateNextReview(Flashcard card){
        int box = card.getBox();
        int days = intervals[box];

        LocalDate nextDate = LocalDate.now().plusDays(days);
        long timestamp = nextDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;

        card.setNextReview(timestamp);
        String timeStr = Study.getTimeUntilReview(card);
        System.out.println("Next review: " + timeStr);
    }
}
