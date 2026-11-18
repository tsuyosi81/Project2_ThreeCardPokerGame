package edu.uic.cs342.poker.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckDealerTest {

    @Test
    void newDeckHas52Cards() {
        assertEquals(52, new Deck().size());
    }

    @Test
    void newDeckHasNoDuplicates() {
        assertEquals(52, new HashSet<>(new Deck()).size());
    }

    @Test
    void eachSuitHas13Cards() {
        Deck deck = new Deck();
        for (char suit : new char[]{'C', 'D', 'H', 'S'}) {
            int count = 0;
            for (Card c : deck) {
                if (c.getSuit() == suit) {
                    count++;
                }
            }
            assertEquals(13, count);
        }
    }

    @Test
    void valuesGoFrom2To14() {
        for (Card c : new Deck()) {
            assertTrue(c.getValue() >= 2 && c.getValue() <= 14);
        }
    }

    @Test
    void shuffleChangesOrder() {
        Deck deck = new Deck(new Random(1));
        ArrayList<Card> before = new ArrayList<>(deck);
        deck.shuffle();
        assertNotEquals(before, new ArrayList<>(deck));
        assertEquals(52, deck.size());
    }

    @Test
    void resetPutsAllCardsBack() {
        Deck deck = new Deck();
        deck.remove(0);
        deck.remove(0);
        deck.reset();
        assertEquals(52, deck.size());
        assertEquals(52, new HashSet<>(deck).size());
    }

    @Test
    void dealHandGivesThreeCards() {
        assertEquals(3, new Dealer().dealHand().size());
    }

    @Test
    void dealingRemovesCardsFromDeck() {
        Dealer dealer = new Dealer();
        dealer.dealHand();
        assertEquals(49, dealer.getDeck().size());
    }

    @Test
    void handsDoNotShareCards() {
        Dealer dealer = new Dealer();
        HashSet<Card> seen = new HashSet<>();
        seen.addAll(dealer.dealHand());
        seen.addAll(dealer.dealHand());
        dealer.dealOwnHand();
        seen.addAll(dealer.getDealersHand());
        assertEquals(9, seen.size());
    }

    @Test
    void dealtCardsAreNoLongerInDeck() {
        Dealer dealer = new Dealer();
        ArrayList<Card> hand = dealer.dealHand();
        for (Card c : hand) {
            assertFalse(dealer.getDeck().contains(c));
        }
    }

    @Test
    void dealerGetsNewDeckWhenLow() {
        Dealer dealer = new Dealer();
        for (int i = 0; i < 6; i++) {   // 18 cards gone, 34 left
            dealer.dealHand();
        }
        dealer.dealHand();              // deck refilled before this deal
        assertEquals(49, dealer.getDeck().size());
    }

    @Test
    void dealerHandStartsEmpty() {
        assertTrue(new Dealer().getDealersHand().isEmpty());
    }

    @Test
    void cardRejectsBadValues() {
        assertThrows(IllegalArgumentException.class, () -> new Card('S', 1));
        assertThrows(IllegalArgumentException.class, () -> new Card('S', 15));
        assertThrows(IllegalArgumentException.class, () -> new Card('X', 5));
    }

    @Test
    void cardText() {
        assertEquals("A", new Card('S', 14).getRankText());
        assertEquals("10", new Card('H', 10).getRankText());
        assertTrue(new Card('H', 5).isRed());
        assertFalse(new Card('C', 5).isRed());
    }
}
