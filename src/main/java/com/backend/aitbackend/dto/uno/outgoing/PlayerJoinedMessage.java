package com.backend.aitbackend.dto.uno.outgoing;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.backend.aitbackend.model.uno.Room;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerJoinedMessage extends WebSocketMessage {
    @JsonProperty("playerName")
    private String playerName;
    
    @JsonProperty("room")
    private Room room;

    public PlayerJoinedMessage() {
        super("PLAYER_JOINED");
    }

    public PlayerJoinedMessage(String playerName, Room room) {
        this();
        this.playerName = playerName;
        this.room = room;
    }

    // Getters and Setters
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
