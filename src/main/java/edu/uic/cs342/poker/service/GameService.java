package edu.uic.cs342.poker.service;

import edu.uic.cs342.poker.logic.ThreeCardLogic;
import edu.uic.cs342.poker.model.Card;
import edu.uic.cs342.poker.model.Dealer;
import edu.uic.cs342.poker.model.Player;

import java.util.ArrayList;

/**
 * Runs the game: bets, dealing, Play/Fold, reveal and payouts.
 * Controllers call this class. It does not know about JavaFX.
 */
public class GameService {
    public static final int MIN_BET = 5;
    public static final int MAX_BET = 25;

    private final Player[] players = {new Player("Player 1"), new Player("Player 2")};
    private final Dealer dealer;
    private final PlayerResult[] results = new PlayerResult[2];
    private Phase phase = Phase.BETTING;
    private int round = 1;
    private boolean dealerQualified;

    public GameService() {
        this(new Dealer());
    }

    // tests pass in a dealer with a known deck
    public GameService(Dealer dealer) {
        this.dealer = dealer;
    }

    // ---------- getters ----------

    public Player getPlayer(int index) {
        return players[index];
    }

    public Dealer getDealer() {
        return dealer;
    }

    public Phase getPhase() {
        return phase;
    }

    public int getRound() {
        return round;
    }

    public boolean didDealerQualify() {
        return dealerQualified;
    }

    public PlayerResult getResult(int index) {
        return results[index];
    }

    /** Name of a player's hand, e.g. "Pair" or "King-high". Empty before the deal. */
    public String getHandName(int index) {
        ArrayList<Card> hand = players[index].getHand();
        return hand.isEmpty() ? "" : ThreeCardLogic.handName(hand);
    }

    public String getDealerHandName() {
        ArrayList<Card> hand = dealer.getDealersHand();
        return hand.isEmpty() ? "" : ThreeCardLogic.handName(hand);
    }

    /** Which player (0 or 1) is choosing Play/Fold right now, or -1 if nobody. */
    public int getActivePlayer() {
        if (phase == Phase.PLAYER1_DECIDE) {
            return 0;
        }
        if (phase == Phase.PLAYER2_DECIDE) {
            return 1;
        }
        return -1;
    }

    // ---------- betting ----------

    public static boolean isValidAnte(int amount) {
        return amount >= MIN_BET && amount <= MAX_BET;
    }

    /** Pair Plus is optional, so 0 is allowed. */
    public static boolean isValidPairPlus(int amount) {
        return amount == 0 || isValidAnte(amount);
    }

    /**
     * Saves one player's bets. If their ante was carried over from last round,
     * the ante they already have stays and the ante argument is ignored.
     */
    public void placeBets(int index, int ante, int pairPlus) {
        if (phase != Phase.BETTING) {
            throw new IllegalStateException("Bets can only be placed before the deal");
        }
        Player p = players[index];
        if (!p.isAnteCarried()) {
            if (!isValidAnte(ante)) {
                throw new IllegalArgumentException("Ante must be between $5 and $25");
            }
            p.setAnteBet(ante);
        }
        if (!isValidPairPlus(pairPlus)) {
            throw new IllegalArgumentException("Pair Plus must be blank or between $5 and $25");
        }
        p.setPairPlusBet(pairPlus);
    }

    public boolean canDeal() {
        return phase == Phase.BETTING
                && isValidAnte(players[0].getAnteBet())
                && isValidAnte(players[1].getAnteBet());
    }

    // ---------- round steps ----------

    public void deal() {
        if (!canDeal()) {
            throw new IllegalStateException("Both players need a valid Ante before dealing");
        }
        players[0].setHand(dealer.dealHand());
        players[1].setHand(dealer.dealHand());
        dealer.dealOwnHand();
        phase = Phase.PLAYER1_DECIDE;
    }

    public void play(int index) {
        checkTurn(index);
        Player p = players[index];
        p.setPlayBet(p.getAnteBet());
        nextTurn();
    }

    public void fold(int index) {
        checkTurn(index);
        players[index].setFolded(true);
        nextTurn();
    }

    /** How much a player loses by folding: their Ante plus Pair Plus. */
    public int foldLoss(int index) {
        return players[index].getAnteBet() + players[index].getPairPlusBet();
    }

    /** Shows the dealer's hand, works out every result and updates total winnings. */
    public void revealDealer() {
        if (phase != Phase.READY_TO_REVEAL) {
            throw new IllegalStateException("Both players must choose Play or Fold first");
        }
        ArrayList<Card> dealerHand = dealer.getDealersHand();
        dealerQualified = ThreeCardLogic.dealerQualifies(dealerHand);

        for (int i = 0; i < 2; i++) {
            results[i] = settle(players[i], dealerHand);
            players[i].addWinnings(results[i].getTotalChange());
        }
        phase = Phase.ROUND_OVER;
    }

    public void nextRound() {
        if (phase != Phase.ROUND_OVER) {
            throw new IllegalStateException("Finish the current round first");
        }
        for (Player p : players) {
            p.clearForNewRound();
        }
        results[0] = null;
        results[1] = null;
        dealerQualified = false;
        round++;
        phase = Phase.BETTING;
    }

    // ---------- helpers ----------

    private void checkTurn(int index) {
        if (getActivePlayer() != index) {
            throw new IllegalStateException("It is not " + players[index].getName() + "'s turn");
        }
    }

    private void nextTurn() {
        if (phase == Phase.PLAYER1_DECIDE) {
            phase = Phase.PLAYER2_DECIDE;
        } else {
            phase = Phase.READY_TO_REVEAL;
        }
    }

    private PlayerResult settle(Player p, ArrayList<Card> dealerHand) {
        String hand = ThreeCardLogic.handName(p.getHand());
        int ante = p.getAnteBet();
        int pp = p.getPairPlusBet();
        boolean hasPP = pp > 0;

        if (p.isFolded()) {
            p.setAnteCarried(false);
            return new PlayerResult(PlayerResult.Outcome.FOLDED, hand, -ante, 0, -pp, hasPP);
        }

        // Pair Plus does not care about the dealer
        int ppChange = 0;
        if (hasPP) {
            int won = ThreeCardLogic.evalPPWinnings(p.getHand(), pp);
            ppChange = won > 0 ? won : -pp;
        }

        if (!dealerQualified) {
            // Play bet comes back, Ante stays on the table for next round
            p.setAnteCarried(true);
            return new PlayerResult(PlayerResult.Outcome.DEALER_NOT_QUALIFIED, hand, 0, 0, ppChange, hasPP);
        }

        p.setAnteCarried(false);
        int play = p.getPlayBet();
        int winner = ThreeCardLogic.compareHands(dealerHand, p.getHand());
        if (winner == ThreeCardLogic.PLAYER_WINS) {
            return new PlayerResult(PlayerResult.Outcome.WIN, hand, ante, play, ppChange, hasPP);
        }
        if (winner == ThreeCardLogic.DEALER_WINS) {
            return new PlayerResult(PlayerResult.Outcome.LOSE, hand, -ante, -play, ppChange, hasPP);
        }
        return new PlayerResult(PlayerResult.Outcome.TIE, hand, 0, 0, ppChange, hasPP);
    }
}
