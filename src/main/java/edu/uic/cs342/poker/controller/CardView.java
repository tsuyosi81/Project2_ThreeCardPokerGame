package edu.uic.cs342.poker.controller;

import edu.uic.cs342.poker.model.Card;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Builds the little card boxes shown on screen. Looks come from CSS. */
public class CardView {

    public static StackPane front(Card card) {
        Label rank = new Label(card.getRankText());
        rank.getStyleClass().add("card-rank");
        Label suit = new Label(card.getSuitSymbol());
        suit.getStyleClass().add("card-suit");

        VBox box = new VBox(rank, suit);
        box.getStyleClass().add("card-face");

        StackPane pane = new StackPane(box);
        pane.getStyleClass().addAll("card", card.isRed() ? "card-red" : "card-black");
        pane.setAccessibleText(card.getRankText() + " of " + suitName(card.getSuit()));
        return pane;
    }

    public static StackPane back() {
        Label q = new Label("?");
        q.getStyleClass().add("card-back-mark");
        StackPane pane = new StackPane(q);
        pane.getStyleClass().addAll("card", "card-back");
        pane.setAccessibleText("Hidden card");
        return pane;
    }

    public static StackPane slot() {
        StackPane pane = new StackPane();
        pane.getStyleClass().addAll("card", "card-slot");
        return pane;
    }

    /** Turns a face-down card into a face-up one (used halfway through the flip). */
    public static void showFace(StackPane pane, Card card) {
        StackPane face = front(card);
        pane.getChildren().setAll(new java.util.ArrayList<>(face.getChildren()));
        pane.getStyleClass().setAll(face.getStyleClass());
        pane.setAccessibleText(face.getAccessibleText());
    }

    private static String suitName(char suit) {
        switch (suit) {
            case 'C': return "Clubs";
            case 'D': return "Diamonds";
            case 'H': return "Hearts";
            default: return "Spades";
        }
    }
}
