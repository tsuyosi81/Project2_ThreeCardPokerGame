package edu.uic.cs342.poker.controller;

import edu.uic.cs342.poker.model.Card;
import edu.uic.cs342.poker.model.Player;
import edu.uic.cs342.poker.navigation.Dialogs;
import edu.uic.cs342.poker.navigation.SceneManager;
import edu.uic.cs342.poker.service.GameService;
import edu.uic.cs342.poker.service.PlayerResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/** Results screen: who won, how each bet paid out, and the new totals. */
public class ResultsController {
    @FXML private Button themeButton;
    @FXML private Button playAgainButton;
    @FXML private Label titleLabel;
    @FXML private HBox winnerBanner;
    @FXML private Label bannerTag;
    @FXML private Label bannerText;
    @FXML private HBox dealerCards;
    @FXML private Label dealerInfo;
    @FXML private GridPane table;

    private GameService game;

    @FXML
    private void initialize() {
        game = SceneManager.getGame();
        themeButton.setText(SceneManager.themeButtonText());
        titleLabel.setText("Round " + game.getRound() + " results");

        for (Card c : game.getDealer().getDealersHand()) {
            StackPane card = CardView.front(c);
            card.getStyleClass().add("card-small");
            dealerCards.getChildren().add(card);
        }
        dealerInfo.setText(game.getDealerHandName()
                + (game.didDealerQualify() ? " · Dealer qualified" : " · Dealer did NOT qualify"));

        fillBanner();
        fillTable();

        Animations.pulse(winnerBanner);
        Platform.runLater(() -> playAgainButton.requestFocus());
    }

    private void fillBanner() {
        boolean anyWin = false;
        boolean allLose = true;
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            PlayerResult r = game.getResult(i);
            String name = game.getPlayer(i).getName();
            String change = GameController.money(r.getTotalChange());
            switch (r.getOutcome()) {
                case WIN:
                    anyWin = true;
                    allLose = false;
                    text.append(name).append(" wins with ").append(article(r.getHandName())).append(" (").append(change).append("). ");
                    break;
                case LOSE:
                    text.append(name).append(" loses with ").append(article(r.getHandName())).append(" (").append(change).append("). ");
                    break;
                case FOLDED:
                    text.append(name).append(" folded (").append(change).append("). ");
                    break;
                case TIE:
                    allLose = false;
                    text.append(name).append(" ties the dealer (").append(change).append("). ");
                    break;
                default:
                    allLose = false;
                    text.append(name).append("'s Play bet was returned (").append(change).append("). ");
            }
        }
        if (!game.didDealerQualify()) {
            text.append("The dealer did not qualify, so Antes stay for the next hand.");
        }
        bannerText.setText(text.toString().trim());

        winnerBanner.getStyleClass().removeAll("status-ok", "status-err", "status-info");
        if (anyWin) {
            bannerTag.setText("WINNER");
            winnerBanner.getStyleClass().add("status-ok");
        } else if (allLose) {
            bannerTag.setText("DEALER WINS");
            winnerBanner.getStyleClass().add("status-err");
        } else {
            bannerTag.setText("RESULT");
            winnerBanner.getStyleClass().add("status-info");
        }
    }

    private void fillTable() {
        String[] headers = {"PLAYER", "HAND", "VS DEALER", "ANTE", "PLAY", "PAIR PLUS", "THIS ROUND", "TOTAL WINNINGS"};
        for (int col = 0; col < headers.length; col++) {
            Label h = new Label(headers[col]);
            h.getStyleClass().addAll("cell", "cell-header");
            h.setMaxWidth(Double.MAX_VALUE);
            table.add(h, col, 0);
        }
        for (int i = 0; i < 2; i++) {
            Player p = game.getPlayer(i);
            PlayerResult r = game.getResult(i);
            String vs;
            switch (r.getOutcome()) {
                case WIN: vs = "Win"; break;
                case LOSE: vs = "Lose"; break;
                case TIE: vs = "Tie"; break;
                case FOLDED: vs = "Folded"; break;
                default: vs = "Push";
            }
            String[] values = {
                    p.getName(),
                    handText(p) + " · " + r.getHandName(),
                    vs,
                    GameController.money(r.getAnteChange()),
                    r.getOutcome() == PlayerResult.Outcome.FOLDED ? "—" : GameController.money(r.getPlayChange()),
                    r.placedPairPlus() ? GameController.money(r.getPairPlusChange()) : "No bet",
                    GameController.money(r.getTotalChange()),
                    GameController.money(p.getTotalWinnings())
            };
            for (int col = 0; col < values.length; col++) {
                Label cell = new Label(values[col]);
                cell.getStyleClass().add("cell");
                cell.setMaxWidth(Double.MAX_VALUE);
                if (col == 0 || col == 6 || col == 7) {
                    cell.getStyleClass().add("cell-strong");
                }
                if (col >= 2 && col <= 6) {
                    if (values[col].startsWith("+") || values[col].equals("Win")) {
                        cell.getStyleClass().add("text-ok");
                    } else if (values[col].startsWith("−") || values[col].equals("Lose")) {
                        cell.getStyleClass().add("text-err");
                    }
                }
                table.add(cell, col, i + 1);
            }
        }
    }

    private static String handText(Player p) {
        StringBuilder sb = new StringBuilder();
        for (Card c : p.getHand()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // "a Pair", "an Ace-high", "a Flush"
    private static String article(String handName) {
        char first = Character.toLowerCase(handName.charAt(0));
        boolean vowel = "aeiou8".indexOf(first) >= 0;
        return (vowel ? "an " : "a ") + handName;
    }

    @FXML
    private void handlePlayAgain() {
        game.nextRound();
        SceneManager.show("game.fxml");
    }

    @FXML
    private void handleMainMenu() {
        if (Dialogs.confirmQuit()) {
            SceneManager.show("welcome.fxml");
        }
    }

    @FXML
    private void handleTheme() {
        SceneManager.toggleTheme();
        themeButton.setText(SceneManager.themeButtonText());
    }

    @FXML
    private void handleRules() {
        Dialogs.showRules();
    }
}
