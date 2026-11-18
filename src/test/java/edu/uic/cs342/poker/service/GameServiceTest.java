package edu.uic.cs342.poker.service;

import edu.uic.cs342.poker.model.Card;
import edu.uic.cs342.poker.model.Dealer;
import edu.uic.cs342.poker.model.Deck;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceTest {

    /**
     * Builds a game where the first 9 cards are dealt in this order:
     * player 1 (3 cards), player 2 (3 cards), dealer (3 cards).
     */
    private static GameService gameWith(Card... first9) {
        List<Card> top = Arrays.asList(first9);
        Deck deck = new Deck(new Random(0));
        deck.clear();
        deck.addAll(top);
        for (char s : new char[]{'C', 'D', 'H', 'S'}) {
            for (int v = 2; v <= 14; v++) {
                Card c = new Card(s, v);
                if (!top.contains(c)) {
                    deck.add(c);
                }
            }
        }
        return new GameService(new Dealer(deck));
    }

    private static Card c(char suit, int value) {
        return new Card(suit, value);
    }

    // P1: pair of kings, P2: 7-high, dealer: queen-high (qualifies)
    private static GameService standardGame() {
        return gameWith(
                c('H', 13), c('S', 13), c('D', 4),
                c('C', 7), c('H', 6), c('D', 2),
                c('C', 12), c('D', 9), c('S', 5));
    }

    @Test
    void startsInBettingPhase() {
        GameService g = new GameService();
        assertEquals(Phase.BETTING, g.getPhase());
        assertEquals(1, g.getRound());
    }

    @Test
    void wagerLimits() {
        assertTrue(GameService.isValidAnte(5));
        assertTrue(GameService.isValidAnte(25));
        assertFalse(GameService.isValidAnte(4));
        assertFalse(GameService.isValidAnte(26));
        assertTrue(GameService.isValidPairPlus(0));
        assertFalse(GameService.isValidPairPlus(3));
    }

    @Test
    void badAnteIsRejected() {
        GameService g = new GameService();
        assertThrows(IllegalArgumentException.class, () -> g.placeBets(0, 40, 0));
    }

    @Test
    void cannotDealWithoutBothAntes() {
        GameService g = new GameService();
        g.placeBets(0, 10, 0);
        assertFalse(g.canDeal());
        assertThrows(IllegalStateException.class, g::deal);
    }

    @Test
    void cannotPlayBeforeDeal() {
        GameService g = new GameService();
        assertThrows(IllegalStateException.class, () -> g.play(0));
    }

    @Test
    void playersTakeTurnsInOrder() {
        GameService g = standardGame();
        g.placeBets(0, 10, 5);
        g.placeBets(1, 15, 0);
        g.deal();
        assertEquals(0, g.getActivePlayer());
        assertThrows(IllegalStateException.class, () -> g.play(1));
        g.play(0);
        assertEquals(1, g.getActivePlayer());
        g.fold(1);
        assertEquals(Phase.READY_TO_REVEAL, g.getPhase());
    }

    @Test
    void playBetEqualsAnte() {
        GameService g = standardGame();
        g.placeBets(0, 10, 0);
        g.placeBets(1, 15, 0);
        g.deal();
        g.play(0);
        assertEquals(10, g.getPlayer(0).getPlayBet());
    }

    @Test
    void winAndLosePayouts() {
        GameService g = standardGame();
        g.placeBets(0, 10, 5);
        g.placeBets(1, 15, 0);
        g.deal();
        g.play(0);
        g.play(1);
        g.revealDealer();

        assertTrue(g.didDealerQualify());
        // P1 pair beats queen-high: +10 ante, +10 play, +5 pair plus
        assertEquals(PlayerResult.Outcome.WIN, g.getResult(0).getOutcome());
        assertEquals(25, g.getPlayer(0).getTotalWinnings());
        // P2 7-high loses: -15 ante, -15 play
        assertEquals(PlayerResult.Outcome.LOSE, g.getResult(1).getOutcome());
        assertEquals(-30, g.getPlayer(1).getTotalWinnings());
    }

    @Test
    void foldLosesAnteAndPairPlus() {
        GameService g = standardGame();
        g.placeBets(0, 10, 5);
        g.placeBets(1, 15, 0);
        g.deal();
        g.fold(0);
        g.play(1);
        g.revealDealer();
        assertEquals(PlayerResult.Outcome.FOLDED, g.getResult(0).getOutcome());
        assertEquals(-15, g.getPlayer(0).getTotalWinnings());
    }

    @Test
    void dealerNotQualifyingReturnsPlayAndCarriesAnte() {
        // dealer has jack-high
        GameService g = gameWith(
                c('H', 13), c('S', 13), c('D', 4),
                c('C', 7), c('H', 6), c('D', 2),
                c('S', 11), c('H', 8), c('C', 3));
        g.placeBets(0, 10, 5);
        g.placeBets(1, 15, 0);
        g.deal();
        g.play(0);
        g.play(1);
        g.revealDealer();

        assertFalse(g.didDealerQualify());
        assertEquals(PlayerResult.Outcome.DEALER_NOT_QUALIFIED, g.getResult(0).getOutcome());
        assertEquals(5, g.getPlayer(0).getTotalWinnings());   // only the pair plus win
        assertEquals(0, g.getPlayer(1).getTotalWinnings());

        g.nextRound();
        assertTrue(g.getPlayer(0).isAnteCarried());
        assertEquals(10, g.getPlayer(0).getAnteBet());
        g.placeBets(0, 20, 0);                       // carried ante cannot be changed
        assertEquals(10, g.getPlayer(0).getAnteBet());
    }

    @Test
    void nextRoundOnlyAfterRoundIsOver() {
        GameService g = standardGame();
        assertThrows(IllegalStateException.class, g::nextRound);
        g.placeBets(0, 10, 0);
        g.placeBets(1, 10, 0);
        g.deal();
        g.play(0);
        g.play(1);
        g.revealDealer();
        g.nextRound();
        assertEquals(Phase.BETTING, g.getPhase());
        assertEquals(2, g.getRound());
        assertTrue(g.getPlayer(0).getHand().isEmpty());
        assertEquals(20, g.getPlayer(0).getTotalWinnings());  // winnings kept (+10 ante, +10 play)
    }

    @Test
    void cannotRevealBeforeEveryoneDecides() {
        GameService g = standardGame();
        g.placeBets(0, 10, 0);
        g.placeBets(1, 10, 0);
        g.deal();
        g.play(0);
        assertThrows(IllegalStateException.class, g::revealDealer);
    }

    @Test
    void dealGivesEveryoneThreeCards() {
        GameService g = standardGame();
        g.placeBets(0, 10, 0);
        g.placeBets(1, 10, 0);
        g.deal();
        ArrayList<Card> p1 = g.getPlayer(0).getHand();
        assertEquals(3, p1.size());
        assertEquals(3, g.getPlayer(1).getHand().size());
        assertEquals(3, g.getDealer().getDealersHand().size());
        assertEquals(c('H', 13), p1.get(0));
    }
}
