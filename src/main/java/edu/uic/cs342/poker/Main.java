package edu.uic.cs342.poker;

import edu.uic.cs342.poker.navigation.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager.init(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
