package org.aistudytool.aistudytool;

public class Flashcard {
    private String question;
    private String answer;
    private long nextReview;
    private int box = 1;


    public Flashcard(String question, String answer) {
        this.question = question;
        this.nextReview = 0;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public long getNextReview() {
        return nextReview;
    }

    public void setNextReview(long nextReview) {
        this.nextReview = nextReview;
    }

    public int getBox() {
        return box;
    }

    public void setBox(int box) {
        this.box = box;
    }

    private void createFlashcard(){

    }

    private void previewFC(){

    }

    private void editFC(){

    }

    public void displayFCSets(){

    }

    public void displayAllFC(){

    }
}
