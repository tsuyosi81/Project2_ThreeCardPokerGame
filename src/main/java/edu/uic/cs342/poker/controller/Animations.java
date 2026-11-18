package edu.uic.cs342.poker.controller;

import edu.uic.cs342.poker.navigation.SceneManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.List;

/**
 * The game's animations. Each one has a job:
 * 1. dealCards  - cards slide in one at a time so players see who gets which cards
 * 2. flipCards  - dealer cards turn over one by one so the reveal is easy to follow
 * 3. pulse      - the winner banner and result boxes grow for a moment to draw the eye
 * When "Reduce motion" is on, nothing moves and the end state is shown right away.
 */
public class Animations {

    /** Slides the cards in from above, one after another. */
    public static void dealCards(List<Node> cards, Runnable done) {
        if (SceneManager.isReduceMotion()) {
            done.run();
            return;
        }
        SequentialTransition all = new SequentialTransition();
        for (Node card : cards) {
            card.setOpacity(0);
            card.setTranslateY(-60);

            TranslateTransition move = new TranslateTransition(Duration.millis(220), card);
            move.setToY(0);
            FadeTransition fade = new FadeTransition(Duration.millis(220), card);
            fade.setToValue(1);
            all.getChildren().add(new ParallelTransition(move, fade));
        }
        all.setOnFinished(e -> done.run());
        all.play();
    }

    /**
     * Flips each card: it shrinks to a line, the face is swapped in, and it grows back.
     * flipOne.run(i) is called at the middle of card i's flip to show its face.
     */
    public static void flipCards(List<Node> cards, java.util.function.IntConsumer flipOne, Runnable done) {
        if (SceneManager.isReduceMotion()) {
            for (int i = 0; i < cards.size(); i++) {
                flipOne.accept(i);
            }
            done.run();
            return;
        }
        SequentialTransition all = new SequentialTransition();
        for (int i = 0; i < cards.size(); i++) {
            final int index = i;
            Node card = cards.get(i);

            ScaleTransition shrink = new ScaleTransition(Duration.millis(150), card);
            shrink.setFromX(1);
            shrink.setToX(0);
            shrink.setOnFinished(e -> flipOne.accept(index));

            ScaleTransition grow = new ScaleTransition(Duration.millis(150), card);
            grow.setFromX(0);
            grow.setToX(1);

            all.getChildren().addAll(shrink, grow, new PauseTransition(Duration.millis(120)));
        }
        all.setOnFinished(e -> done.run());
        all.play();
    }

    /** Grows a node a little and shrinks it back. */
    public static void pulse(Node node) {
        if (SceneManager.isReduceMotion()) {
            return;
        }
        ScaleTransition grow = new ScaleTransition(Duration.millis(220), node);
        grow.setFromX(1);
        grow.setFromY(1);
        grow.setToX(1.08);
        grow.setToY(1.08);
        grow.setAutoReverse(true);
        grow.setCycleCount(4);
        grow.play();
    }

    /** Fades a popup in. */
    public static void fadeIn(Node node) {
        if (SceneManager.isReduceMotion()) {
            node.setOpacity(1);
            return;
        }
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setToValue(1);
        fade.play();
    }
}
