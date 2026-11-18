package edu.uic.cs342.poker.model;

/**
 * One playing card. Suit is 'C', 'D', 'H' or 'S'.
 * Value goes from 2 to 14 (11 = Jack, 12 = Queen, 13 = King, 14 = Ace).
 */
public class Card {
    private final char suit;
    private final int value;

    public Card(char suit, int value) {
        if (suit != 'C' && suit != 'D' && suit != 'H' && suit != 'S') {
            throw new IllegalArgumentException("Bad suit: " + suit);
        }
        if (value < 2 || value > 14) {
            throw new IllegalArgumentException("Bad value: " + value);
        }
        this.suit = suit;
        this.value = value;
    }

    public char getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    public boolean isRed() {
        return suit == 'H' || suit == 'D';
    }

    // "A", "K", "10", etc.
    public String getRankText() {
        switch (value) {
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            case 14: return "A";
            default: return String.valueOf(value);
        }
    }

    public String getSuitSymbol() {
        switch (suit) {
            case 'C': return "♣";
            case 'D': return "♦";
            case 'H': return "♥";
            default: return "♠";
        }
    }

    @Override
    public String toString() {
        return getRankText() + getSuitSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Card)) {
            return false;
        }
        Card other = (Card) o;
        return suit == other.suit && value == other.value;
    }

    @Override
    public int hashCode() {
        return suit * 31 + value;
    }
}
