package com.backend.aitbackend.dto.uno.outgoing;

import java.util.List;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.backend.aitbackend.model.uno.Room;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoomsListMessage extends WebSocketMessage {
    @JsonProperty("rooms")
    private List<Room> rooms;

    public RoomsListMessage() {
        super("ROOMS_LIST");
    }

    public RoomsListMessage(List<Room> rooms) {
        this();
        this.rooms = rooms;
    }

    // Getters and Setters
    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
