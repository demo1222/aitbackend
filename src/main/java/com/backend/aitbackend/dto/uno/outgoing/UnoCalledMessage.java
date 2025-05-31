package com.backend.aitbackend.dto.uno.outgoing;

public class UnoCalledMessage {
    private String type = "UNO_CALLED";
    private String roomId;
    private String playerId;
    private String playerName;
    private boolean success;

    public UnoCalledMessage() {}

    public UnoCalledMessage(String roomId, String playerId, String playerName, boolean success) {
        this.roomId = roomId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.success = success;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
