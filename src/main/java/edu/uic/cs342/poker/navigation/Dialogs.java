package edu.uic.cs342.poker.navigation;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.util.Optional;

/** The Quit confirmation and the Rules popup. */
public class Dialogs {

    /** Asks before leaving a game. Returns true only if the player picks "Quit to Menu". */
    public static boolean confirmQuit() {
        ButtonType keep = new ButtonType("Keep Playing", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType quit = new ButtonType("Quit to Menu", ButtonBar.ButtonData.OK_DONE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", keep, quit);
        alert.setTitle("Quit this game?");
        alert.setHeaderText("Quit this game?");
        alert.setContentText("Both players' bets and winnings from this game will be lost. This can't be undone.");
        style(alert);

        // Keep Playing is the safe choice, so Enter and Esc both keep the game
        ((Button) alert.getDialogPane().lookupButton(quit)).setDefaultButton(false);
        ((Button) alert.getDialogPane().lookupButton(keep)).setDefaultButton(true);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == quit;
    }

    public static void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Play");
        alert.setHeaderText("How to Play Three Card Poker");
        Label text = new Label(
                "Two players each play against the dealer.\n\n"
                + "1. Bet: each player places an Ante ($5 to $25). Pair Plus ($5 to $25) is optional.\n"
                + "2. Deal: everyone gets 3 cards. The dealer's cards stay hidden.\n"
                + "3. Decide: Play (bet the same as your Ante again) or Fold (lose your Ante and Pair Plus).\n"
                + "4. Reveal: the dealer needs Queen-high or better to qualify.\n"
                + "     Not qualified: Play bets come back and Antes stay for the next hand.\n"
                + "     Qualified: beat the dealer to win Ante and Play 1:1, or lose both.\n\n"
                + "Hands, best to worst: Straight Flush, Three of a Kind, Straight, Flush, Pair, High Card.\n\n"
                + "Pair Plus pays no matter what the dealer has:\n"
                + "Straight Flush 40:1   Three of a Kind 30:1   Straight 6:1   Flush 3:1   Pair 1:1");
        text.setWrapText(true);
        text.setMaxWidth(560);
        alert.getDialogPane().setContent(text);
        style(alert);
        alert.showAndWait();
    }

    private static void style(Alert alert) {
        alert.initOwner(SceneManager.getStage());
        alert.getDialogPane().getStylesheets().setAll(SceneManager.stylesheets());
    }
}
