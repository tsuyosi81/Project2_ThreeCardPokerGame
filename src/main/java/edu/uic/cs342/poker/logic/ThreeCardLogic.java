package edu.uic.cs342.poker.logic;

import edu.uic.cs342.poker.model.Card;

import java.util.ArrayList;
import java.util.Collections;

/**
 * All the Three Card Poker rules. No JavaFX in here.
 *
 * evalHand returns:      0 = high card, 1 = straight flush, 2 = three of a kind,
 *                        3 = straight, 4 = flush, 5 = pair
 * compareHands returns:  0 = tie (neither hand won), 1 = dealer wins, 2 = player wins
 */
public class ThreeCardLogic {
    public static final int HIGH_CARD = 0;
    public static final int STRAIGHT_FLUSH = 1;
    public static final int THREE_OF_A_KIND = 2;
    public static final int STRAIGHT = 3;
    public static final int FLUSH = 4;
    public static final int PAIR = 5;

    public static final int TIE = 0;
    public static final int DEALER_WINS = 1;
    public static final int PLAYER_WINS = 2;

    private ThreeCardLogic() {
    }

    public static int evalHand(ArrayList<Card> hand) {
        ArrayList<Integer> v = sortedValues(hand);
        boolean flush = hand.get(0).getSuit() == hand.get(1).getSuit()
                && hand.get(1).getSuit() == hand.get(2).getSuit();
        boolean straight = isStraight(v);

        if (straight && flush) {
            return STRAIGHT_FLUSH;
        }
        if (v.get(0).equals(v.get(1)) && v.get(1).equals(v.get(2))) {
            return THREE_OF_A_KIND;
        }
        if (straight) {
            return STRAIGHT;
        }
        if (flush) {
            return FLUSH;
        }
        if (v.get(0).equals(v.get(1)) || v.get(1).equals(v.get(2))) {
            return PAIR;
        }
        return HIGH_CARD;
    }

    /** Returns the amount won on the Pair Plus bet (not counting the bet itself). 0 if it lost. */
    public static int evalPPWinnings(ArrayList<Card> hand, int bet) {
        switch (evalHand(hand)) {
            case STRAIGHT_FLUSH: return bet * 40;
            case THREE_OF_A_KIND: return bet * 30;
            case STRAIGHT: return bet * 6;
            case FLUSH: return bet * 3;
            case PAIR: return bet;
            default: return 0;
        }
    }

    public static int compareHands(ArrayList<Card> dealer, ArrayList<Card> player) {
        int dRank = strength(evalHand(dealer));
        int pRank = strength(evalHand(player));
        if (pRank > dRank) {
            return PLAYER_WINS;
        }
        if (dRank > pRank) {
            return DEALER_WINS;
        }

        // same kind of hand, so compare the cards
        ArrayList<Integer> d = tieBreakOrder(dealer);
        ArrayList<Integer> p = tieBreakOrder(player);
        for (int i = 0; i < 3; i++) {
            if (p.get(i) > d.get(i)) {
                return PLAYER_WINS;
            }
            if (d.get(i) > p.get(i)) {
                return DEALER_WINS;
            }
        }
        return TIE;
    }

    /** Dealer needs Queen high or better to qualify. */
    public static boolean dealerQualifies(ArrayList<Card> dealer) {
        if (evalHand(dealer) != HIGH_CARD) {
            return true;
        }
        return sortedValues(dealer).get(2) >= 12;
    }

    public static String handName(ArrayList<Card> hand) {
        switch (evalHand(hand)) {
            case STRAIGHT_FLUSH: return "Straight Flush";
            case THREE_OF_A_KIND: return "Three of a Kind";
            case STRAIGHT: return "Straight";
            case FLUSH: return "Flush";
            case PAIR: return "Pair";
            default: return highCardName(sortedValues(hand).get(2)) + "-high";
        }
    }

    // ---------- helpers ----------

    // turns the evalHand code into a number where bigger = better hand
    private static int strength(int code) {
        switch (code) {
            case STRAIGHT_FLUSH: return 6;
            case THREE_OF_A_KIND: return 5;
            case STRAIGHT: return 4;
            case FLUSH: return 3;
            case PAIR: return 2;
            default: return 1;
        }
    }

    private static ArrayList<Integer> sortedValues(ArrayList<Card> hand) {
        ArrayList<Integer> v = new ArrayList<>();
        for (Card c : hand) {
            v.add(c.getValue());
        }
        Collections.sort(v);
        return v;
    }

    // A-2-3 counts as a straight (the ace is low there)
    private static boolean isStraight(ArrayList<Integer> v) {
        if (v.get(0) == 2 && v.get(1) == 3 && v.get(2) == 14) {
            return true;
        }
        return v.get(1) == v.get(0) + 1 && v.get(2) == v.get(1) + 1;
    }

    /**
     * Card values in the order they should be compared.
     * Pair: pair value first, then the kicker. A-2-3 straight: counts as 3-high.
     * Everything else: highest card first.
     */
    private static ArrayList<Integer> tieBreakOrder(ArrayList<Card> hand) {
        ArrayList<Integer> v = sortedValues(hand);
        ArrayList<Integer> order = new ArrayList<>();

        if (v.get(0) == 2 && v.get(1) == 3 && v.get(2) == 14) {
            order.add(3);
            order.add(2);
            order.add(1);
            return order;
        }
        if (v.get(0).equals(v.get(1)) && !v.get(1).equals(v.get(2))) {
            order.add(v.get(0));
            order.add(v.get(0));
            order.add(v.get(2));
            return order;
        }
        if (v.get(1).equals(v.get(2)) && !v.get(0).equals(v.get(1))) {
            order.add(v.get(2));
            order.add(v.get(2));
            order.add(v.get(0));
            return order;
        }
        order.add(v.get(2));
        order.add(v.get(1));
        order.add(v.get(0));
        return order;
    }

    private static String highCardName(int value) {
        switch (value) {
            case 11: return "Jack";
            case 12: return "Queen";
            case 13: return "King";
            case 14: return "Ace";
            default: return String.valueOf(value);
        }
    }
}
