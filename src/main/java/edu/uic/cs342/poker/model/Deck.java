package edu.uic.cs342.poker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/** A standard 52 card deck. */
public class Deck extends ArrayList<Card> {
    private static final char[] SUITS = {'C', 'D', 'H', 'S'};
    private final Random random;

    public Deck() {
        this(new Random());
    }

    // lets tests use a fixed seed
    public Deck(Random random) {
        this.random = random;
        newDeck();
    }

    /** Clears the deck, puts all 52 cards back, and shuffles. */
    public void newDeck() {
        clear();
        for (char suit : SUITS) {
            for (int value = 2; value <= 14; value++) {
                add(new Card(suit, value));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(this, random);
    }

    /** Same as newDeck, named for the spec's "reset". */
    public void reset() {
        newDeck();
    }
}
