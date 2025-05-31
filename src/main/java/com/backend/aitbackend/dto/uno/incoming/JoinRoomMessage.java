package com.backend.aitbackend.dto.uno.incoming;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JoinRoomMessage extends WebSocketMessage {
    @JsonProperty("playerName")
    private String playerName;
    
    @JsonProperty("roomId")
    private String roomId;

    public JoinRoomMessage() {
        super("JOIN_ROOM");
    }

    // Getters and Setters
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
