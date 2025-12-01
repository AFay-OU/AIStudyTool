package org.aistudytool.aistudytool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            URL url = getClass().getResource("flashcardFrame.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load(), 800, 600);
            stage.setResizable(false);
            stage.setTitle("AI Study Tool");
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

