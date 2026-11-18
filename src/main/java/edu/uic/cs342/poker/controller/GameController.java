package edu.uic.cs342.poker.controller;

import edu.uic.cs342.poker.model.Card;
import edu.uic.cs342.poker.model.Player;
import edu.uic.cs342.poker.navigation.Dialogs;
import edu.uic.cs342.poker.navigation.SceneManager;
import edu.uic.cs342.poker.service.GameService;
import edu.uic.cs342.poker.service.Phase;
import edu.uic.cs342.poker.service.PlayerResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Game screen. Reads the state from GameService and shows it.
 * All rules and payouts are in GameService / ThreeCardLogic, not here.
 */
public class GameController {
    @FXML private Button themeButton;
    @FXML private Button mainButton;
    @FXML private Label actionHint;
    @FXML private Label roundLabel;
    @FXML private Label step0;
    @FXML private Label step1;
    @FXML private Label step2;
    @FXML private Label step3;
    @FXML private Label step4;
    @FXML private HBox statusBar;
    @FXML private Label statusTag;
    @FXML private Label statusText;
    @FXML private HBox dealerCards;
    @FXML private Label dealerInfo;
    @FXML private VBox overlay;
    @FXML private HBox overlayBoxes;

    @FXML private VBox p1Panel;
    @FXML private VBox p2Panel;
    @FXML private Label p1Total;
    @FXML private Label p2Total;
    @FXML private Label p1Tag;
    @FXML private Label p2Tag;
    @FXML private HBox p1Cards;
    @FXML private HBox p2Cards;
    @FXML private Label p1HandName;
    @FXML private Label p2HandName;
    @FXML private TextField p1Ante;
    @FXML private TextField p2Ante;
    @FXML private TextField p1PP;
    @FXML private TextField p2PP;
    @FXML private Label p1AnteHint;
    @FXML private Label p2AnteHint;
    @FXML private Label p1PPHint;
    @FXML private Label p2PPHint;
    @FXML private Label p1PlayAmount;
    @FXML private Label p2PlayAmount;
    @FXML private Button p1Play;
    @FXML private Button p2Play;
    @FXML private Button p1Fold;
    @FXML private Button p2Fold;

    private GameService game;
    private boolean animating = false;

    // the same controls for player 1 and 2, so we can loop over them
    private VBox[] panels;
    private Label[] totals;
    private Label[] tags;
    private HBox[] cardBoxes;
    private Label[] handNames;
    private TextField[] antes;
    private TextField[] pairPlus;
    private Label[] anteHints;
    private Label[] ppHints;
    private Label[] playAmounts;
    private Button[] playButtons;
    private Button[] foldButtons;
    private Label[] steps;

    @FXML
    private void initialize() {
        game = SceneManager.getGame();

        panels = new VBox[]{p1Panel, p2Panel};
        totals = new Label[]{p1Total, p2Total};
        tags = new Label[]{p1Tag, p2Tag};
        cardBoxes = new HBox[]{p1Cards, p2Cards};
        handNames = new Label[]{p1HandName, p2HandName};
        antes = new TextField[]{p1Ante, p2Ante};
        pairPlus = new TextField[]{p1PP, p2PP};
        anteHints = new Label[]{p1AnteHint, p2AnteHint};
        ppHints = new Label[]{p1PPHint, p2PPHint};
        playAmounts = new Label[]{p1PlayAmount, p2PlayAmount};
        playButtons = new Button[]{p1Play, p2Play};
        foldButtons = new Button[]{p1Fold, p2Fold};
        steps = new Label[]{step0, step1, step2, step3, step4};

        themeButton.setText(SceneManager.themeButtonText());

        for (int i = 0; i < 2; i++) {
            onlyDigits(antes[i]);
            onlyDigits(pairPlus[i]);
            antes[i].setAccessibleText("Player " + (i + 1) + " Ante, 5 to 25 dollars");
            pairPlus[i].setAccessibleText("Player " + (i + 1) + " Pair Plus, optional, 5 to 25 dollars");
            antes[i].textProperty().addListener((obs, oldText, newText) -> updateControls());
            pairPlus[i].textProperty().addListener((obs, oldText, newText) -> updateControls());
        }
        // Esc or Enter also closes the result popup
        overlay.setFocusTraversable(true);
        overlay.setOnKeyPressed(e -> hideOverlay());

        fillBetFields();
        drawCards();
        updateControls();
        Platform.runLater(() -> {
            int first = firstEditableAnte();
            if (first >= 0) {
                antes[first].requestFocus();
            } else {
                pairPlus[0].requestFocus();
            }
        });
    }

    // ================= button handlers =================

    @FXML
    private void handleMainButton() {
        Phase phase = game.getPhase();
        if (phase == Phase.BETTING) {
            deal();
        } else if (phase == Phase.READY_TO_REVEAL) {
            reveal();
        } else if (phase == Phase.ROUND_OVER) {
            SceneManager.show("results.fxml");
        }
    }

    @FXML
    private void handleP1Play() {
        decide(0, true);
    }

    @FXML
    private void handleP1Fold() {
        decide(0, false);
    }

    @FXML
    private void handleP2Play() {
        decide(1, true);
    }

    @FXML
    private void handleP2Fold() {
        decide(1, false);
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

    @FXML
    private void handleQuit() {
        if (Dialogs.confirmQuit()) {
            SceneManager.show("welcome.fxml");
        }
    }

    @FXML
    private void handleOverlayClick() {
        hideOverlay();
    }

    // ================= round steps =================

    private void deal() {
        for (int i = 0; i < 2; i++) {
            game.placeBets(i, readNumber(antes[i]), readNumber(pairPlus[i]));
        }
        game.deal();
        fillBetFields();
        drawCards();

        // deal order: player 1, player 2, dealer, then around again
        List<Node> order = new ArrayList<>();
        for (int c = 0; c < 3; c++) {
            order.add(p1Cards.getChildren().get(c));
            order.add(p2Cards.getChildren().get(c));
            order.add(dealerCards.getChildren().get(c));
        }
        animating = true;
        updateControls();
        Animations.dealCards(order, () -> {
            animating = false;
            updateControls();
            p1Play.requestFocus();
        });
    }

    private void decide(int index, boolean play) {
        if (play) {
            game.play(index);
        } else {
            game.fold(index);
        }
        fillBetFields();
        drawCards();   // player 2's cards turn face up when it is their turn
        updateControls();

        if (!play) {
            showOverlay(foldBox(index));
        } else if (game.getPhase() == Phase.PLAYER2_DECIDE) {
            p2Play.requestFocus();
        } else {
            mainButton.requestFocus();
        }
    }

    private void reveal() {
        game.revealDealer();
        animating = true;
        updateControls();

        ArrayList<Card> hand = game.getDealer().getDealersHand();
        List<Node> cards = new ArrayList<>(dealerCards.getChildren());
        Animations.flipCards(cards,
                i -> CardView.showFace((StackPane) cards.get(i), hand.get(i)),
                () -> {
                    animating = false;
                    updateControls();
                    showOverlay(resultBox(0), resultBox(1));
                });
    }

    // ================= drawing =================

    /** Rebuilds the card pictures for both players and the dealer. */
    private void drawCards() {
        Phase phase = game.getPhase();
        for (int i = 0; i < 2; i++) {
            Player p = game.getPlayer(i);
            cardBoxes[i].getChildren().clear();
            // player 2's cards stay hidden while player 1 decides
            boolean hidden = (i == 1 && phase == Phase.PLAYER1_DECIDE);
            for (int c = 0; c < 3; c++) {
                if (p.getHand().isEmpty()) {
                    cardBoxes[i].getChildren().add(CardView.slot());
                } else if (hidden) {
                    cardBoxes[i].getChildren().add(CardView.back());
                } else {
                    cardBoxes[i].getChildren().add(CardView.front(p.getHand().get(c)));
                }
            }
            cardBoxes[i].setOpacity(p.isFolded() ? 0.4 : 1);
        }

        dealerCards.getChildren().clear();
        ArrayList<Card> dealerHand = game.getDealer().getDealersHand();
        for (int c = 0; c < 3; c++) {
            if (phase == Phase.BETTING) {
                dealerCards.getChildren().add(CardView.slot());
            } else if (phase == Phase.ROUND_OVER) {
                dealerCards.getChildren().add(CardView.front(dealerHand.get(c)));
            } else {
                dealerCards.getChildren().add(CardView.back());
            }
        }
    }

    /** Puts the saved bets into the fields (used after the deal and for carried antes). */
    private void fillBetFields() {
        for (int i = 0; i < 2; i++) {
            Player p = game.getPlayer(i);
            if (game.getPhase() == Phase.BETTING) {
                if (p.isAnteCarried()) {
                    antes[i].setText(String.valueOf(p.getAnteBet()));
                }
            } else {
                antes[i].setText(String.valueOf(p.getAnteBet()));
                pairPlus[i].setText(p.getPairPlusBet() > 0 ? String.valueOf(p.getPairPlusBet()) : "");
            }
        }
    }

    /** Updates every label, tag and button for the current phase. */
    private void updateControls() {
        Phase phase = game.getPhase();
        int active = game.getActivePlayer();

        roundLabel.setText("ROUND " + game.getRound());
        updateSteps(phase);

        for (int i = 0; i < 2; i++) {
            Player p = game.getPlayer(i);
            totals[i].setText(money(p.getTotalWinnings()));
            setActive(panels[i], i == active && !animating);

            boolean betting = phase == Phase.BETTING;
            antes[i].setDisable(!betting || p.isAnteCarried() || animating);
            pairPlus[i].setDisable(!betting || animating);

            boolean myTurn = i == active && !animating;
            playButtons[i].setDisable(!myTurn);
            foldButtons[i].setDisable(!myTurn);
            playButtons[i].setText(p.getAnteBet() > 0 ? "Play $" + p.getAnteBet() : "Play");
            playAmounts[i].setText(p.getPlayBet() > 0 ? "$" + p.getPlayBet() : "—");

            boolean showHand = !p.getHand().isEmpty() && !(i == 1 && phase == Phase.PLAYER1_DECIDE);
            handNames[i].setText(showHand ? game.getHandName(i) : (p.getHand().isEmpty() ? "" : "Hidden until your turn"));

            if (betting) {
                updateBetHints(i);
            } else {
                setHint(anteHints[i], p.isAnteCarried() && phase == Phase.ROUND_OVER ? "Carries over" : "Locked", false);
                setHint(ppHints[i], p.getPairPlusBet() > 0 ? "Locked" : "No bet", false);
            }
            updateTag(i);
        }

        updateDealerInfo(phase);
        updateStatusAndMainButton(phase);
    }

    private void updateSteps(Phase phase) {
        int current;
        switch (phase) {
            case BETTING: current = 0; break;
            case PLAYER1_DECIDE:
            case PLAYER2_DECIDE: current = 2; break;
            case READY_TO_REVEAL: current = 3; break;
            default: current = 4;
        }
        String[] names = {"Bet", "Deal", "Decide", "Reveal", "Payout"};
        for (int s = 0; s < 5; s++) {
            boolean done = s < current || (phase == Phase.ROUND_OVER);
            steps[s].setText((done && s != current ? "✓ " : "") + (s + 1) + " " + names[s]);
            setStyle(steps[s], "step-current", s == current);
            setStyle(steps[s], "step-done", done && s != current);
        }
    }

    private void updateBetHints(int i) {
        Player p = game.getPlayer(i);
        if (p.isAnteCarried()) {
            setHint(anteHints[i], "Carried over from last round", false);
        } else {
            boolean bad = !antes[i].getText().isEmpty() && !GameService.isValidAnte(readNumber(antes[i]));
            setHint(anteHints[i], bad ? "Enter $5–$25" : "$5–$25", bad);
            setStyle(antes[i], "field-error", bad);
        }
        boolean badPP = !GameService.isValidPairPlus(readNumber(pairPlus[i]));
        setHint(ppHints[i], badPP ? "Enter $5–$25, or leave blank" : "Optional · leave blank to skip", badPP);
        setStyle(pairPlus[i], "field-error", badPP);
    }

    private void updateTag(int i) {
        Player p = game.getPlayer(i);
        Phase phase = game.getPhase();
        if (phase == Phase.BETTING) {
            if (betReady(i)) {
                setTag(tags[i], "READY", "tag-ok");
            } else if (hasBadInput(i)) {
                setTag(tags[i], "FIX BET", "tag-err");
            } else {
                setTag(tags[i], "ENTER BET", "tag-info");
            }
        } else if (phase == Phase.ROUND_OVER && game.getResult(i) != null) {
            PlayerResult r = game.getResult(i);
            String style;
            switch (r.getOutcome()) {
                case WIN: style = "tag-ok"; break;
                case LOSE: style = "tag-err"; break;
                case FOLDED: style = "tag-mute"; break;
                default: style = "tag-info";
            }
            setTag(tags[i], r.getOutcome() == PlayerResult.Outcome.DEALER_NOT_QUALIFIED ? "PUSH" : r.getLabel(), style);
        } else if (p.isFolded()) {
            setTag(tags[i], "FOLDED", "tag-mute");
        } else if (i == game.getActivePlayer()) {
            setTag(tags[i], "YOUR TURN", "tag-info");
        } else if (p.getPlayBet() > 0) {
            setTag(tags[i], "PLAYING", "tag-ok");
        } else {
            setTag(tags[i], "WAITING", "tag-mute");
        }
    }

    private void updateDealerInfo(Phase phase) {
        setStyle(dealerInfo, "text-ok", false);
        setStyle(dealerInfo, "text-err", false);
        if (phase == Phase.BETTING) {
            dealerInfo.setText("Dealer qualifies with Queen-high or better");
        } else if (phase != Phase.ROUND_OVER || animating) {
            dealerInfo.setText("Dealer's cards stay hidden until both players decide");
        } else if (game.didDealerQualify()) {
            dealerInfo.setText(game.getDealerHandName() + " · Dealer qualifies");
            setStyle(dealerInfo, "text-ok", true);
        } else {
            dealerInfo.setText(game.getDealerHandName() + " · Dealer does NOT qualify");
            setStyle(dealerInfo, "text-err", true);
        }
    }

    private void updateStatusAndMainButton(Phase phase) {
        actionHint.setText("");
        mainButton.setDisable(animating);

        if (phase == Phase.BETTING) {
            mainButton.setText("Deal Cards");
            String error = firstBetError();
            if (error != null) {
                setStatus("ERROR", error, "status-err");
            } else {
                String text = "Both players: enter an Ante ($5–$25). Pair Plus is optional. Then press Deal Cards.";
                for (int i = 0; i < 2; i++) {
                    Player p = game.getPlayer(i);
                    if (p.isAnteCarried()) {
                        text += " " + p.getName() + "'s $" + p.getAnteBet() + " Ante carried over from last round.";
                    }
                }
                setStatus("NEXT", text, "status-info");
            }
            boolean ready = betReady(0) && betReady(1);
            mainButton.setDisable(!ready || animating);
            if (!ready) {
                actionHint.setText(!betReady(0) ? "Enter Player 1's Ante to unlock" : "Enter Player 2's Ante to unlock");
            }
        } else if (phase == Phase.PLAYER1_DECIDE || phase == Phase.PLAYER2_DECIDE) {
            mainButton.setText("Reveal Dealer");
            mainButton.setDisable(true);
            actionHint.setText("Unlocks after both players decide");
            int i = game.getActivePlayer();
            Player p = game.getPlayer(i);
            String text = p.getName() + ": check your hand, then Play (bet $" + p.getAnteBet()
                    + " more) or Fold (lose your Ante and Pair Plus).";
            if (i == 1 && game.getPlayer(0).isFolded()) {
                text = "Player 1 folded and lost $" + game.foldLoss(0) + ". " + text;
            }
            if (animating) {
                text = "Dealing cards…";
            }
            setStatus("NEXT", text, "status-info");
        } else if (phase == Phase.READY_TO_REVEAL) {
            mainButton.setText("Reveal Dealer");
            setStatus("NEXT", "Both players have decided. Press Reveal Dealer to see the dealer's hand.", "status-info");
        } else {
            mainButton.setText("See Results");
            if (animating) {
                setStatus("NEXT", "Revealing the dealer's cards…", "status-info");
            } else if (game.didDealerQualify()) {
                setStatus("RESULT", "Dealer has " + game.getDealerHandName() + " and qualifies. "
                        + outcomeSentence(0) + " " + outcomeSentence(1), "status-ok");
            } else {
                setStatus("RESULT", "Dealer has only " + game.getDealerHandName()
                        + " and does not qualify. Play bets are returned; Antes stay for the next hand.", "status-info");
            }
        }
    }

    // ================= result popups =================

    private VBox resultBox(int i) {
        PlayerResult r = game.getResult(i);
        String title;
        String detail;
        String style;
        switch (r.getOutcome()) {
            case WIN: style = "result-win"; break;
            case LOSE: style = "result-lose"; break;
            case FOLDED: style = "result-fold"; break;
            default: style = "result-push";
        }
        if (r.getOutcome() == PlayerResult.Outcome.FOLDED) {
            title = "FOLDED  " + money(r.getTotalChange());
            detail = "Ante and Pair Plus were lost.";
        } else if (r.getOutcome() == PlayerResult.Outcome.DEALER_NOT_QUALIFIED) {
            title = "PLAY RETURNED  " + money(r.getTotalChange());
            detail = "Play $" + game.getPlayer(i).getPlayBet() + " returned · Ante stays for next hand · " + ppText(r);
        } else {
            title = r.getLabel() + "  " + money(r.getTotalChange());
            detail = "Ante " + money(r.getAnteChange()) + " · Play " + money(r.getPlayChange()) + " · " + ppText(r);
        }
        return popupBox(game.getPlayer(i).getName() + " · " + r.getHandName(), title, detail, style,
                r.getOutcome() == PlayerResult.Outcome.WIN);
    }

    private VBox foldBox(int i) {
        String detail = "Ante and Pair Plus are lost. " + game.getPlayer(i).getName() + " sits out until the next round.";
        return popupBox(game.getPlayer(i).getName(), "FOLDED  " + money(-game.foldLoss(i)), detail, "result-fold", false);
    }

    private VBox popupBox(String who, String title, String detail, String style, boolean emphasize) {
        Label whoLabel = new Label(who);
        whoLabel.getStyleClass().add("result-who");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("result-title");
        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("result-detail");
        detailLabel.setWrapText(true);

        VBox box = new VBox(6, whoLabel, titleLabel, detailLabel);
        box.getStyleClass().addAll("result-box", style);
        box.setMaxWidth(340);
        box.setPrefWidth(340);
        if (emphasize) {
            box.getProperties().put("pulse", Boolean.TRUE);
        }
        return box;
    }

    private void showOverlay(VBox... boxes) {
        overlayBoxes.getChildren().setAll(boxes);
        overlay.setVisible(true);
        Animations.fadeIn(overlay);
        for (VBox b : boxes) {
            if (b.getProperties().containsKey("pulse")) {
                Animations.pulse(b);
            }
        }
        overlay.requestFocus();
    }

    private void hideOverlay() {
        if (!overlay.isVisible()) {
            return;
        }
        overlay.setVisible(false);
        int active = game.getActivePlayer();
        if (active >= 0) {
            playButtons[active].requestFocus();
        } else {
            mainButton.requestFocus();
        }
    }

    // ================= small helpers =================

    private boolean betReady(int i) {
        Player p = game.getPlayer(i);
        boolean anteOk = p.isAnteCarried() || GameService.isValidAnte(readNumber(antes[i]));
        return anteOk && GameService.isValidPairPlus(readNumber(pairPlus[i]));
    }

    private boolean hasBadInput(int i) {
        boolean badAnte = !game.getPlayer(i).isAnteCarried() && !antes[i].getText().isEmpty()
                && !GameService.isValidAnte(readNumber(antes[i]));
        return badAnte || !GameService.isValidPairPlus(readNumber(pairPlus[i]));
    }

    private String firstBetError() {
        for (int i = 0; i < 2; i++) {
            String name = game.getPlayer(i).getName();
            if (!game.getPlayer(i).isAnteCarried() && !antes[i].getText().isEmpty()
                    && !GameService.isValidAnte(readNumber(antes[i]))) {
                return name + "'s Ante must be between $5 and $25. You entered $" + antes[i].getText() + ".";
            }
            if (!GameService.isValidPairPlus(readNumber(pairPlus[i]))) {
                return name + "'s Pair Plus must be between $5 and $25, or left blank. You entered $"
                        + pairPlus[i].getText() + ".";
            }
        }
        return null;
    }

    private int firstEditableAnte() {
        for (int i = 0; i < 2; i++) {
            if (!antes[i].isDisabled()) {
                return i;
            }
        }
        return -1;
    }

    private String outcomeSentence(int i) {
        PlayerResult r = game.getResult(i);
        String name = game.getPlayer(i).getName();
        switch (r.getOutcome()) {
            case WIN: return name + " wins.";
            case LOSE: return name + " loses.";
            case TIE: return name + " ties the dealer.";
            default: return name + " folded.";
        }
    }

    private static String ppText(PlayerResult r) {
        return r.placedPairPlus() ? "Pair Plus " + money(r.getPairPlusChange()) : "No Pair Plus bet";
    }

    /** "+$25", "−$30" or "$0". */
    static String money(int amount) {
        if (amount > 0) {
            return "+$" + amount;
        }
        if (amount < 0) {
            return "−$" + (-amount);
        }
        return "$0";
    }

    private static int readNumber(TextField field) {
        String text = field.getText();
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    // only lets the user type up to 3 digits
    private static void onlyDigits(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,3}") ? change : null));
    }

    private void setStatus(String tag, String text, String style) {
        statusTag.setText(tag);
        statusText.setText(text);
        statusBar.getStyleClass().removeAll("status-info", "status-ok", "status-err");
        statusBar.getStyleClass().add(style);
    }

    private static void setTag(Label tag, String text, String style) {
        tag.setText(text);
        tag.getStyleClass().removeAll("tag-ok", "tag-info", "tag-err", "tag-mute");
        tag.getStyleClass().add(style);
    }

    private static void setHint(Label hint, String text, boolean error) {
        hint.setText(text);
        setStyle(hint, "hint-error", error);
    }

    private static void setActive(VBox panel, boolean active) {
        setStyle(panel, "active", active);
    }

    private static void setStyle(Node node, String styleClass, boolean on) {
        node.getStyleClass().remove(styleClass);
        if (on) {
            node.getStyleClass().add(styleClass);
        }
    }
}
