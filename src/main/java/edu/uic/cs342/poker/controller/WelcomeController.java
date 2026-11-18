package edu.uic.cs342.poker.controller;

import edu.uic.cs342.poker.model.Card;
import edu.uic.cs342.poker.navigation.Dialogs;
import edu.uic.cs342.poker.navigation.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

public class WelcomeController {
    @FXML private HBox heroCards;
    @FXML private Button themeButton;
    @FXML private CheckBox reduceMotionBox;
    @FXML private Button startButton;

    @FXML
    private void initialize() {
        heroCards.getChildren().addAll(
                CardView.front(new Card('S', 14)),
                CardView.front(new Card('H', 13)),
                CardView.front(new Card('D', 12)));
        themeButton.setText(SceneManager.themeButtonText());
        reduceMotionBox.setSelected(SceneManager.isReduceMotion());
        Platform.runLater(() -> startButton.requestFocus());
    }

    @FXML
    private void handleStart() {
        SceneManager.newGame();
        SceneManager.show("game.fxml");
    }

    @FXML
    private void handleRules() {
        Dialogs.showRules();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleTheme() {
        SceneManager.toggleTheme();
        themeButton.setText(SceneManager.themeButtonText());
    }

    @FXML
    private void handleReduceMotion() {
        SceneManager.setReduceMotion(reduceMotionBox.isSelected());
    }
}
