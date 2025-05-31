package com.backend.aitbackend.dto.uno.incoming;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateRoomMessage extends WebSocketMessage {
    @JsonProperty("playerName")
    private String playerName;
    
    @JsonProperty("roomName")
    private String roomName;
    
    @JsonProperty("maxPlayers")
    private int maxPlayers = 4;
    
    @JsonProperty("isPrivate")
    private boolean isPrivate = false;

    public CreateRoomMessage() {
        super("CREATE_ROOM");
    }

    // Getters and Setters
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
