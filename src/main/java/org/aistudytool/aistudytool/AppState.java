package org.aistudytool.aistudytool;

public class AppState {
    private static final RedoState redoState = new RedoState();
    public static RedoState getRedoState() {
        return redoState;
    }
}
