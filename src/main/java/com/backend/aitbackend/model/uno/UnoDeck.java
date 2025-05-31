package com.backend.aitbackend.model.uno;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UnoDeck {
    private List<UnoCard> cards;
    private Random random;

    public UnoDeck() {
        this.cards = new ArrayList<>();
        this.random = new Random();
        initializeDeck();
    }

    private void initializeDeck() {
        // Add number cards (0-9) for each color
        for (UnoCard.Color color : Arrays.asList(UnoCard.Color.RED, UnoCard.Color.YELLOW, 
                                                 UnoCard.Color.GREEN, UnoCard.Color.BLUE)) {
            // Add one 0 card for each color
            cards.add(new UnoCard(color, UnoCard.Type.ZERO));
            
            // Add two cards of each number (1-9) for each color
            for (UnoCard.Type type : Arrays.asList(UnoCard.Type.ONE, UnoCard.Type.TWO, UnoCard.Type.THREE,
                                                   UnoCard.Type.FOUR, UnoCard.Type.FIVE, UnoCard.Type.SIX,
                                                   UnoCard.Type.SEVEN, UnoCard.Type.EIGHT, UnoCard.Type.NINE)) {
                cards.add(new UnoCard(color, type));
                cards.add(new UnoCard(color, type)); // Two of each
            }
            
            // Add action cards (2 of each per color)
            for (UnoCard.Type type : Arrays.asList(UnoCard.Type.SKIP, UnoCard.Type.REVERSE, UnoCard.Type.DRAW_TWO)) {
                cards.add(new UnoCard(color, type));
                cards.add(new UnoCard(color, type)); // Two of each
            }
        }
        
        // Add Wild cards (4 of each)
        for (int i = 0; i < 4; i++) {
            cards.add(new UnoCard(UnoCard.Color.WILD, UnoCard.Type.WILD));
            cards.add(new UnoCard(UnoCard.Color.WILD, UnoCard.Type.WILD_DRAW_FOUR));
        }
    }

    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    public UnoCard drawCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot draw from empty deck");
        }
        return cards.remove(cards.size() - 1);
    }

    public List<UnoCard> drawCards(int count) {
        List<UnoCard> drawnCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (!cards.isEmpty()) {
                drawnCards.add(drawCard());
            }
        }
        return drawnCards;
    }

    public void addCard(UnoCard card) {
        cards.add(card);
    }

    public void addCards(List<UnoCard> cardsToAdd) {
        cards.addAll(cardsToAdd);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void reset() {
        cards.clear();
        initializeDeck();
        shuffle();
    }

    // For debugging purposes
    public List<UnoCard> getCards() {
        return new ArrayList<>(cards);
    }
}
