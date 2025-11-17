package org.aistudytool.aistudytool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainTest extends Application {

    @Override
    public void start(Stage stage) {
        try {
            URL url = getClass().getResource("flashcardFrame.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load(), 640, 480);
            stage.setTitle("Flashcard Study Tool");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        public static void main (String[]args){
            launch(args);
        }
    }

