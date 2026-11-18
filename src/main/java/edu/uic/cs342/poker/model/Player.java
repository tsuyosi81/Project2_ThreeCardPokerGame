package edu.uic.cs342.poker.model;

import java.util.ArrayList;

/** One player: their cards, their bets and their total winnings. */
public class Player {
    private final String name;
    private ArrayList<Card> hand = new ArrayList<>();
    private int anteBet;
    private int pairPlusBet;
    private int playBet;
    private int totalWinnings;
    private boolean folded;
    private boolean anteCarried; // true when the ante was pushed from last round

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
    }

    public int getAnteBet() {
        return anteBet;
    }

    public void setAnteBet(int anteBet) {
        this.anteBet = anteBet;
    }

    public int getPairPlusBet() {
        return pairPlusBet;
    }

    public void setPairPlusBet(int pairPlusBet) {
        this.pairPlusBet = pairPlusBet;
    }

    public int getPlayBet() {
        return playBet;
    }

    public void setPlayBet(int playBet) {
        this.playBet = playBet;
    }

    public int getTotalWinnings() {
        return totalWinnings;
    }

    public void addWinnings(int amount) {
        totalWinnings += amount;
    }

    public boolean isFolded() {
        return folded;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }

    public boolean isAnteCarried() {
        return anteCarried;
    }

    public void setAnteCarried(boolean anteCarried) {
        this.anteCarried = anteCarried;
    }

    /** Clears the hand and bets for a new round. Keeps the ante if it was carried over. */
    public void clearForNewRound() {
        hand = new ArrayList<>();
        pairPlusBet = 0;
        playBet = 0;
        folded = false;
        if (!anteCarried) {
            anteBet = 0;
        }
    }
}
