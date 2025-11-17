package org.aistudytool.aistudytool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class FlashcardStorageHandler {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveFC(FlashcardController set, String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            gson.toJson(set, fw);
        }
    }

    public static FlashcardController loadFC(String filename) throws IOException {
        try (FileReader fr = new FileReader(filename)) {
            return gson.fromJson(fr, FlashcardController.class);
        }
    }

    private void viewFC(){

    }

    private void pickSet(){

    }

    private void fetchDue(){

    }
}
