package com.backend.aitbackend.dto.uno.outgoing;

import java.util.List;
import java.util.Map;

import com.backend.aitbackend.model.uno.UnoCard;
import com.backend.aitbackend.model.uno.UnoGameState;

public class GameStateMessage {
    private String type = "GAME_STATE";
    private String roomId;
    private UnoCard topCard;
    private UnoCard.Color declaredColor;
    private String currentPlayerId;
    private UnoGameState.Direction direction;
    private Map<String, Integer> playerHandSizes;
    private List<UnoCard> yourHand; // Only for the receiving player
    private int cardsToDrawNext;
    private boolean gameActive;
    private String winnerId;
    private int deckSize;

    public GameStateMessage() {}

    public GameStateMessage(String roomId, UnoCard topCard, UnoCard.Color declaredColor, 
                           String currentPlayerId, UnoGameState.Direction direction,
                           Map<String, Integer> playerHandSizes, int cardsToDrawNext,
                           boolean gameActive, String winnerId, int deckSize) {
        this.roomId = roomId;
        this.topCard = topCard;
        this.declaredColor = declaredColor;
        this.currentPlayerId = currentPlayerId;
        this.direction = direction;
        this.playerHandSizes = playerHandSizes;
        this.cardsToDrawNext = cardsToDrawNext;
        this.gameActive = gameActive;
        this.winnerId = winnerId;
        this.deckSize = deckSize;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public UnoCard getTopCard() {
        return topCard;
    }

    public void setTopCard(UnoCard topCard) {
        this.topCard = topCard;
    }

    public UnoCard.Color getDeclaredColor() {
        return declaredColor;
    }

    public void setDeclaredColor(UnoCard.Color declaredColor) {
        this.declaredColor = declaredColor;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public UnoGameState.Direction getDirection() {
        return direction;
    }

    public void setDirection(UnoGameState.Direction direction) {
        this.direction = direction;
    }

    public Map<String, Integer> getPlayerHandSizes() {
        return playerHandSizes;
    }

    public void setPlayerHandSizes(Map<String, Integer> playerHandSizes) {
        this.playerHandSizes = playerHandSizes;
    }

    public List<UnoCard> getYourHand() {
        return yourHand;
    }

    public void setYourHand(List<UnoCard> yourHand) {
        this.yourHand = yourHand;
    }

    public int getCardsToDrawNext() {
        return cardsToDrawNext;
    }

    public void setCardsToDrawNext(int cardsToDrawNext) {
        this.cardsToDrawNext = cardsToDrawNext;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void setGameActive(boolean gameActive) {
        this.gameActive = gameActive;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public int getDeckSize() {
        return deckSize;
    }

    public void setDeckSize(int deckSize) {
        this.deckSize = deckSize;
    }
}
