package com.backend.aitbackend.dto.uno.incoming;

public class DrawCardMessage {
    private String type = "DRAW_CARD";
    private String playerId;
    private String roomId;
    private int count;

    public DrawCardMessage() {}

    public DrawCardMessage(String playerId, String roomId, int count) {
        this.playerId = playerId;
        this.roomId = roomId;
        this.count = count;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
