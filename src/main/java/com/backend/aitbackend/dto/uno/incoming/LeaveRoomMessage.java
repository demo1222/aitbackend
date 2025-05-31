package com.backend.aitbackend.dto.uno.incoming;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LeaveRoomMessage extends WebSocketMessage {
    @JsonProperty("playerId")
    private String playerId;

    public LeaveRoomMessage() {
        super("LEAVE_ROOM");
    }

    // Getters and Setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
