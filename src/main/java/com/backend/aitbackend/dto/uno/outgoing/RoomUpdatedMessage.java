package com.backend.aitbackend.dto.uno.outgoing;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.backend.aitbackend.model.uno.Room;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoomUpdatedMessage extends WebSocketMessage {
    @JsonProperty("room")
    private Room room;

    public RoomUpdatedMessage() {
        super("ROOM_UPDATED");
    }

    public RoomUpdatedMessage(Room room) {
        this();
        this.room = room;
    }

    // Getters and Setters
    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
