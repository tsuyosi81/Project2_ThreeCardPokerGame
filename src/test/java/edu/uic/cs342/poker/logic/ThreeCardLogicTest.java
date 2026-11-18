package edu.uic.cs342.poker.logic;

import edu.uic.cs342.poker.model.Card;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreeCardLogicTest {

    // makes a hand from strings like "AS", "10H", "QD"
    private static ArrayList<Card> hand(String... cards) {
        ArrayList<Card> h = new ArrayList<>();
        for (String s : cards) {
            String rank = s.substring(0, s.length() - 1);
            char suit = s.charAt(s.length() - 1);
            int value;
            switch (rank) {
                case "J": value = 11; break;
                case "Q": value = 12; break;
                case "K": value = 13; break;
                case "A": value = 14; break;
                default: value = Integer.parseInt(rank);
            }
            h.add(new Card(suit, value));
        }
        return h;
    }

    // ----- evalHand -----

    @Test
    void highCardIsZero() {
        assertEquals(0, ThreeCardLogic.evalHand(hand("2S", "7H", "KD")));
    }

    @Test
    void straightFlushIsOne() {
        assertEquals(1, ThreeCardLogic.evalHand(hand("5H", "6H", "7H")));
    }

    @Test
    void threeOfAKindIsTwo() {
        assertEquals(2, ThreeCardLogic.evalHand(hand("9S", "9H", "9D")));
    }

    @Test
    void straightIsThree() {
        assertEquals(3, ThreeCardLogic.evalHand(hand("8S", "9H", "10D")));
    }

    @Test
    void flushIsFour() {
        assertEquals(4, ThreeCardLogic.evalHand(hand("2C", "8C", "KC")));
    }

    @Test
    void pairIsFive() {
        assertEquals(5, ThreeCardLogic.evalHand(hand("4S", "4H", "JD")));
    }

    @Test
    void cardOrderDoesNotMatter() {
        assertEquals(3, ThreeCardLogic.evalHand(hand("10D", "8S", "9H")));
        assertEquals(5, ThreeCardLogic.evalHand(hand("JD", "4S", "4H")));
    }

    @Test
    void aceTwoThreeIsStraight() {
        assertEquals(3, ThreeCardLogic.evalHand(hand("AS", "2H", "3D")));
    }

    @Test
    void queenKingAceIsStraight() {
        assertEquals(3, ThreeCardLogic.evalHand(hand("QS", "KH", "AD")));
    }

    @Test
    void kingAceTwoIsNotStraight() {
        assertEquals(0, ThreeCardLogic.evalHand(hand("KS", "AH", "2D")));
    }

    @Test
    void aceTwoThreeSameSuitIsStraightFlush() {
        assertEquals(1, ThreeCardLogic.evalHand(hand("AD", "2D", "3D")));
    }

    // ----- evalPPWinnings -----

    @Test
    void pairPlusStraightFlushPays40To1() {
        assertEquals(400, ThreeCardLogic.evalPPWinnings(hand("5H", "6H", "7H"), 10));
    }

    @Test
    void pairPlusThreeOfAKindPays30To1() {
        assertEquals(150, ThreeCardLogic.evalPPWinnings(hand("9S", "9H", "9D"), 5));
    }

    @Test
    void pairPlusStraightPays6To1() {
        assertEquals(60, ThreeCardLogic.evalPPWinnings(hand("8S", "9H", "10D"), 10));
    }

    @Test
    void pairPlusFlushPays3To1() {
        assertEquals(75, ThreeCardLogic.evalPPWinnings(hand("2C", "8C", "KC"), 25));
    }

    @Test
    void pairPlusPairPays1To1() {
        assertEquals(15, ThreeCardLogic.evalPPWinnings(hand("4S", "4H", "JD"), 15));
    }

    @Test
    void pairPlusHighCardPaysNothing() {
        assertEquals(0, ThreeCardLogic.evalPPWinnings(hand("2S", "7H", "KD"), 25));
    }

    // ----- compareHands -----

    @Test
    void betterHandTypeWins() {
        // player flush beats dealer pair
        assertEquals(2, ThreeCardLogic.compareHands(hand("4S", "4H", "JD"), hand("2C", "8C", "KC")));
        // dealer straight beats player flush
        assertEquals(1, ThreeCardLogic.compareHands(hand("8S", "9H", "10D"), hand("2C", "8C", "KC")));
    }

    @Test
    void straightBeatsFlush() {
        assertEquals(2, ThreeCardLogic.compareHands(hand("2C", "8C", "KC"), hand("8S", "9H", "10D")));
    }

    @Test
    void higherHighCardWins() {
        assertEquals(2, ThreeCardLogic.compareHands(hand("2S", "7H", "QD"), hand("3S", "5H", "KD")));
    }

    @Test
    void secondCardBreaksHighCardTie() {
        assertEquals(1, ThreeCardLogic.compareHands(hand("2S", "9H", "KD"), hand("3S", "8H", "KC")));
    }

    @Test
    void higherPairWins() {
        assertEquals(2, ThreeCardLogic.compareHands(hand("5S", "5H", "AD"), hand("9S", "9H", "2D")));
    }

    @Test
    void samePairUsesKicker() {
        assertEquals(1, ThreeCardLogic.compareHands(hand("7S", "7H", "QD"), hand("7C", "7D", "10S")));
    }

    @Test
    void sameCardsDifferentSuitsIsTie() {
        assertEquals(0, ThreeCardLogic.compareHands(hand("2S", "7H", "KD"), hand("2C", "7D", "KS")));
    }

    @Test
    void aceTwoThreeIsLowestStraight() {
        assertEquals(1, ThreeCardLogic.compareHands(hand("2S", "3H", "4D"), hand("AS", "2H", "3D")));
    }

    @Test
    void higherStraightFlushWins() {
        assertEquals(2, ThreeCardLogic.compareHands(hand("5H", "6H", "7H"), hand("9S", "10S", "JS")));
    }

    // ----- dealer qualification -----

    @Test
    void queenHighQualifies() {
        assertTrue(ThreeCardLogic.dealerQualifies(hand("2S", "7H", "QD")));
    }

    @Test
    void jackHighDoesNotQualify() {
        assertFalse(ThreeCardLogic.dealerQualifies(hand("2S", "7H", "JD")));
    }

    @Test
    void lowPairQualifies() {
        assertTrue(ThreeCardLogic.dealerQualifies(hand("2S", "2H", "5D")));
    }

    // ----- hand names -----

    @Test
    void handNames() {
        assertEquals("Pair", ThreeCardLogic.handName(hand("4S", "4H", "JD")));
        assertEquals("King-high", ThreeCardLogic.handName(hand("2S", "7H", "KD")));
        assertEquals("Straight Flush", ThreeCardLogic.handName(hand("5H", "6H", "7H")));
    }
}
