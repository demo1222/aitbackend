package com.backend.aitbackend.model.uno;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnoGameState {
    private UnoDeck deck;
    private List<UnoCard> discardPile;
    private Map<String, List<UnoCard>> playerHands;
    private List<String> playerOrder;
    private int currentPlayerIndex;
    private Direction direction;
    private UnoCard topCard;
    private UnoCard.Color declaredColor;
    private boolean gameActive;
    private int cardsToDrawNext;
    private String winnerId;
    private Map<String, Boolean> saidUno;

    public enum Direction {
        CLOCKWISE, COUNTERCLOCKWISE
    }

    public UnoGameState() {
        this.deck = new UnoDeck();
        this.discardPile = new ArrayList<>();
        this.playerHands = new HashMap<>();
        this.playerOrder = new ArrayList<>();
        this.direction = Direction.CLOCKWISE;
        this.gameActive = false;
        this.cardsToDrawNext = 0;
        this.saidUno = new HashMap<>();
    }

    public void initializeGame(List<String> playerIds) {
        // Reset game state
        deck.reset();
        discardPile.clear();
        playerHands.clear();
        playerOrder.clear();
        saidUno.clear();
        
        // Set up players
        playerOrder.addAll(playerIds);
        Collections.shuffle(playerOrder);
        
        // Deal 7 cards to each player
        for (String playerId : playerOrder) {
            List<UnoCard> hand = deck.drawCards(7);
            playerHands.put(playerId, hand);
            saidUno.put(playerId, false);
        }
        
        // Draw first card for discard pile (ensure it's not a wild card)
        UnoCard firstCard;
        do {
            firstCard = deck.drawCard();
        } while (firstCard.isWildCard());
        
        discardPile.add(firstCard);
        topCard = firstCard;
        declaredColor = firstCard.getColor();
        
        // Handle special first card
        if (firstCard.getType() == UnoCard.Type.SKIP) {
            nextPlayer(); // Skip first player
        } else if (firstCard.getType() == UnoCard.Type.REVERSE && playerOrder.size() == 2) {
            nextPlayer(); // In 2-player game, reverse acts like skip
        } else if (firstCard.getType() == UnoCard.Type.DRAW_TWO) {
            cardsToDrawNext = 2;
        }
        
        currentPlayerIndex = 0;
        gameActive = true;
    }

    public boolean canPlayCard(String playerId, UnoCard card) {
        if (!gameActive || !isCurrentPlayer(playerId)) {
            return false;
        }
        
        List<UnoCard> playerHand = playerHands.get(playerId);
        if (!playerHand.contains(card)) {
            return false;
        }
        
        // If player must draw cards, they can only play a matching draw card
        if (cardsToDrawNext > 0) {
            if (card.getType() == UnoCard.Type.DRAW_TWO && topCard.getType() == UnoCard.Type.DRAW_TWO) {
                return true;
            }
            if (card.getType() == UnoCard.Type.WILD_DRAW_FOUR && topCard.getType() == UnoCard.Type.WILD_DRAW_FOUR) {
                return true;
            }
            return false;
        }
        
        return card.canPlayOn(topCard, declaredColor);
    }

    public GamePlayResult playCard(String playerId, UnoCard card, UnoCard.Color newColor) {
        if (!canPlayCard(playerId, card)) {
            return new GamePlayResult(false, "Cannot play this card");
        }
        
        List<UnoCard> playerHand = playerHands.get(playerId);
        playerHand.remove(card);
        discardPile.add(card);
        topCard = card;
        
        GamePlayResult result = new GamePlayResult(true, "Card played successfully");
        result.setPlayedCard(card);
        result.setPlayerId(playerId);
        
        // Handle wild cards
        if (card.isWildCard()) {
            declaredColor = newColor != null ? newColor : UnoCard.Color.RED; // Default to red if null
            result.setDeclaredColor(declaredColor);
        } else {
            declaredColor = card.getColor();
        }
        
        // Check for UNO (one card left)
        if (playerHand.size() == 1) {
            result.setPlayerHasUno(true);
        }
        
        // Check for win
        if (playerHand.isEmpty()) {
            gameActive = false;
            winnerId = playerId;
            result.setGameWon(true);
            result.setWinnerId(playerId);
            return result;
        }
        
        // Handle card effects
        handleCardEffect(card, result);
        
        // Move to next player if no special action occurred
        if (!result.isSkipNextPlayer() && cardsToDrawNext == 0) {
            nextPlayer();
        }
        
        return result;
    }

    private void handleCardEffect(UnoCard card, GamePlayResult result) {
        switch (card.getType()) {
            case SKIP:
                nextPlayer();
                result.setSkipNextPlayer(true);
                break;
            case REVERSE:
                if (playerOrder.size() == 2) {
                    // In 2-player game, reverse acts like skip
                    nextPlayer();
                    result.setSkipNextPlayer(true);
                } else {
                    direction = (direction == Direction.CLOCKWISE) ? 
                               Direction.COUNTERCLOCKWISE : Direction.CLOCKWISE;
                    result.setDirectionReversed(true);
                }
                break;
            case DRAW_TWO:
                cardsToDrawNext += 2;
                result.setCardsToDrawNext(cardsToDrawNext);
                break;
            case WILD_DRAW_FOUR:
                cardsToDrawNext += 4;
                result.setCardsToDrawNext(cardsToDrawNext);
                break;
        }
    }

    public DrawResult drawCards(String playerId, int count) {
        if (!isCurrentPlayer(playerId)) {
            return new DrawResult(false, "Not your turn", Collections.emptyList());
        }
        
        // Ensure deck has enough cards
        ensureDeckHasCards(count);
        
        List<UnoCard> drawnCards = deck.drawCards(count);
        playerHands.get(playerId).addAll(drawnCards);
        
        // Reset cards to draw
        cardsToDrawNext = 0;
        
        // Move to next player
        nextPlayer();
        
        return new DrawResult(true, "Cards drawn", drawnCards);
    }

    private void ensureDeckHasCards(int needed) {
        if (deck.size() >= needed) {
            return;
        }
        
        // Shuffle discard pile back into deck (except top card)
        if (discardPile.size() > 1) {
            UnoCard currentTop = discardPile.remove(discardPile.size() - 1);
            deck.addCards(discardPile);
            deck.shuffle();
            discardPile.clear();
            discardPile.add(currentTop);
        }
    }

    public boolean callUno(String playerId) {
        if (playerHands.get(playerId).size() == 1) {
            saidUno.put(playerId, true);
            return true;
        }
        return false;
    }

    public boolean challengeUno(String challengerId, String challengedId) {
        List<UnoCard> challengedHand = playerHands.get(challengedId);
        Boolean saidUnoFlag = saidUno.get(challengedId);
        
        if (challengedHand.size() == 1 && !saidUnoFlag) {
            // Challenge successful - challenged player draws 2 cards
            ensureDeckHasCards(2);
            challengedHand.addAll(deck.drawCards(2));
            return true;
        }
        
        return false;
    }

    private void nextPlayer() {
        if (direction == Direction.CLOCKWISE) {
            currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
        } else {
            currentPlayerIndex = (currentPlayerIndex - 1 + playerOrder.size()) % playerOrder.size();
        }
    }

    public boolean isCurrentPlayer(String playerId) {
        return playerOrder.get(currentPlayerIndex).equals(playerId);
    }

    public String getCurrentPlayerId() {
        return playerOrder.get(currentPlayerIndex);
    }

    public List<UnoCard> getPlayerHand(String playerId) {
        return new ArrayList<>(playerHands.getOrDefault(playerId, Collections.emptyList()));
    }

    public int getPlayerHandSize(String playerId) {
        return playerHands.getOrDefault(playerId, Collections.emptyList()).size();
    }

    // Getters and Setters
    public UnoCard getTopCard() {
        return topCard;
    }

    public UnoCard.Color getDeclaredColor() {
        return declaredColor;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public int getCardsToDrawNext() {
        return cardsToDrawNext;
    }

    public Direction getDirection() {
        return direction;
    }

    public List<String> getPlayerOrder() {
        return new ArrayList<>(playerOrder);
    }

    public Map<String, Integer> getPlayerHandSizes() {
        Map<String, Integer> sizes = new HashMap<>();
        playerHands.forEach((playerId, hand) -> sizes.put(playerId, hand.size()));
        return sizes;
    }

    public int getDeckSize() {
        return deck.size();
    }

    // Result classes
    public static class GamePlayResult {
        private boolean success;
        private String message;
        private UnoCard playedCard;
        private String playerId;
        private UnoCard.Color declaredColor;
        private boolean skipNextPlayer;
        private boolean directionReversed;
        private boolean gameWon;
        private String winnerId;
        private boolean playerHasUno;
        private int cardsToDrawNext;

        public GamePlayResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UnoCard getPlayedCard() { return playedCard; }
        public void setPlayedCard(UnoCard playedCard) { this.playedCard = playedCard; }
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        public UnoCard.Color getDeclaredColor() { return declaredColor; }
        public void setDeclaredColor(UnoCard.Color declaredColor) { this.declaredColor = declaredColor; }
        public boolean isSkipNextPlayer() { return skipNextPlayer; }
        public void setSkipNextPlayer(boolean skipNextPlayer) { this.skipNextPlayer = skipNextPlayer; }
        public boolean isDirectionReversed() { return directionReversed; }
        public void setDirectionReversed(boolean directionReversed) { this.directionReversed = directionReversed; }
        public boolean isGameWon() { return gameWon; }
        public void setGameWon(boolean gameWon) { this.gameWon = gameWon; }
        public String getWinnerId() { return winnerId; }
        public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
        public boolean isPlayerHasUno() { return playerHasUno; }
        public void setPlayerHasUno(boolean playerHasUno) { this.playerHasUno = playerHasUno; }
        public int getCardsToDrawNext() { return cardsToDrawNext; }
        public void setCardsToDrawNext(int cardsToDrawNext) { this.cardsToDrawNext = cardsToDrawNext; }
    }

    public static class DrawResult {
        private boolean success;
        private String message;
        private List<UnoCard> drawnCards;

        public DrawResult(boolean success, String message, List<UnoCard> drawnCards) {
            this.success = success;
            this.message = message;
            this.drawnCards = drawnCards;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<UnoCard> getDrawnCards() { return drawnCards; }
    }
}
