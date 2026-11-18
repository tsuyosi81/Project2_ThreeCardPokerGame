package edu.uic.cs342.poker.service;

/** The steps of one round. */
public enum Phase {
    BETTING,          // both players enter bets
    PLAYER1_DECIDE,   // cards dealt, player 1 picks Play or Fold
    PLAYER2_DECIDE,   // player 2 picks Play or Fold
    READY_TO_REVEAL,  // both decided, waiting for Reveal Dealer
    ROUND_OVER        // dealer shown, money paid out
}
