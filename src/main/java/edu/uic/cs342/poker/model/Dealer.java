package edu.uic.cs342.poker.model;

import java.util.ArrayList;

/** Holds the deck and deals three card hands. */
public class Dealer {
    private Deck theDeck;
    private ArrayList<Card> dealersHand;

    public Dealer() {
        this(new Deck());
    }

    public Dealer(Deck deck) {
        theDeck = deck;
        dealersHand = new ArrayList<>();
    }

    /** Takes 3 cards off the top of the deck. Gets a fresh deck if fewer than 34 cards are left. */
    public ArrayList<Card> dealHand() {
        if (theDeck.size() <= 34) {
            theDeck.newDeck();
        }
        ArrayList<Card> hand = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            hand.add(theDeck.remove(0));
        }
        return hand;
    }

    /** Deals the dealer's own hand. */
    public void dealOwnHand() {
        dealersHand = dealHand();
    }

    public ArrayList<Card> getDealersHand() {
        return dealersHand;
    }

    public Deck getDeck() {
        return theDeck;
    }
}
