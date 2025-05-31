package com.backend.aitbackend.dto.uno.incoming;

import com.backend.aitbackend.model.uno.UnoCard;

public class PlayCardMessage {
    private String type = "PLAY_CARD";
    private String playerId;
    private String roomId;
    private UnoCard card;
    private UnoCard.Color declaredColor; // For wild cards

    public PlayCardMessage() {}

    public PlayCardMessage(String playerId, String roomId, UnoCard card, UnoCard.Color declaredColor) {
        this.playerId = playerId;
        this.roomId = roomId;
        this.card = card;
        this.declaredColor = declaredColor;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public UnoCard getCard() {
        return card;
    }

    public void setCard(UnoCard card) {
        this.card = card;
    }

    public UnoCard.Color getDeclaredColor() {
        return declaredColor;
    }

    public void setDeclaredColor(UnoCard.Color declaredColor) {
        this.declaredColor = declaredColor;
    }
}
