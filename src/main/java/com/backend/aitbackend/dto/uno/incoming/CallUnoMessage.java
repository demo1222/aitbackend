package com.backend.aitbackend.dto.uno.incoming;

public class CallUnoMessage {
    private String type = "CALL_UNO";
    private String playerId;
    private String roomId;

    public CallUnoMessage() {}

    public CallUnoMessage(String playerId, String roomId) {
        this.playerId = playerId;
        this.roomId = roomId;
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
}
