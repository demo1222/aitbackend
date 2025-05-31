package com.backend.aitbackend.dto.uno.outgoing;

import java.util.List;

import com.backend.aitbackend.model.uno.UnoCard;

public class CardsDrawnMessage {
    private String type = "CARDS_DRAWN";
    private String roomId;
    private String playerId;
    private String playerName;
    private int cardCount;
    private List<UnoCard> drawnCards; // Only visible to the player who drew
    private String nextPlayerId;

    public CardsDrawnMessage() {}

    public CardsDrawnMessage(String roomId, String playerId, String playerName, 
                           int cardCount, String nextPlayerId) {
        this.roomId = roomId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.cardCount = cardCount;
        this.nextPlayerId = nextPlayerId;
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

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public List<UnoCard> getDrawnCards() {
        return drawnCards;
    }

    public void setDrawnCards(List<UnoCard> drawnCards) {
        this.drawnCards = drawnCards;
    }

    public String getNextPlayerId() {
        return nextPlayerId;
    }

    public void setNextPlayerId(String nextPlayerId) {
        this.nextPlayerId = nextPlayerId;
    }
}
