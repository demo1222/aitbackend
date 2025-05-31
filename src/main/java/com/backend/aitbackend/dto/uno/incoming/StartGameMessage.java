package com.backend.aitbackend.dto.uno.incoming;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StartGameMessage extends WebSocketMessage {
    @JsonProperty("roomId")
    private String roomId;

    public StartGameMessage() {
        super("START_GAME");
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
