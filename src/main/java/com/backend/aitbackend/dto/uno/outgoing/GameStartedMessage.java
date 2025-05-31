package com.backend.aitbackend.dto.uno.outgoing;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GameStartedMessage extends WebSocketMessage {
    @JsonProperty("roomId")
    private String roomId;

    public GameStartedMessage() {
        super("GAME_STARTED");
    }

    public GameStartedMessage(String roomId) {
        this();
        this.roomId = roomId;
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
