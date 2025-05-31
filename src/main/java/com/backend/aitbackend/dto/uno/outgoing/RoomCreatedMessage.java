package com.backend.aitbackend.dto.uno.outgoing;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.backend.aitbackend.model.uno.Room;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoomCreatedMessage extends WebSocketMessage {
    @JsonProperty("room")
    private Room room;
    
    @JsonProperty("playerId")
    private String playerId;

    public RoomCreatedMessage() {
        super("ROOM_CREATED");
    }

    public RoomCreatedMessage(Room room, String playerId) {
        this();
        this.room = room;
        this.playerId = playerId;
    }

    // Getters and Setters
    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
