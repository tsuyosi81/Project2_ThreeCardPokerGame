package edu.uic.cs342.poker.service;

/** What happened to one player at the end of a round. Amounts are + for won, - for lost. */
public class PlayerResult {
    public enum Outcome { WIN, LOSE, TIE, FOLDED, DEALER_NOT_QUALIFIED }

    private final Outcome outcome;
    private final String handName;
    private final int anteChange;
    private final int playChange;
    private final int pairPlusChange;
    private final boolean placedPairPlus;

    public PlayerResult(Outcome outcome, String handName, int anteChange, int playChange,
                        int pairPlusChange, boolean placedPairPlus) {
        this.outcome = outcome;
        this.handName = handName;
        this.anteChange = anteChange;
        this.playChange = playChange;
        this.pairPlusChange = pairPlusChange;
        this.placedPairPlus = placedPairPlus;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public String getHandName() {
        return handName;
    }

    public int getAnteChange() {
        return anteChange;
    }

    public int getPlayChange() {
        return playChange;
    }

    public int getPairPlusChange() {
        return pairPlusChange;
    }

    public boolean placedPairPlus() {
        return placedPairPlus;
    }

    public int getTotalChange() {
        return anteChange + playChange + pairPlusChange;
    }

    /** Short word for the result, e.g. "WIN" or "PUSH". */
    public String getLabel() {
        switch (outcome) {
            case WIN: return "WIN";
            case LOSE: return "LOSE";
            case TIE: return "TIE";
            case FOLDED: return "FOLDED";
            default: return "PLAY RETURNED";
        }
    }
}
