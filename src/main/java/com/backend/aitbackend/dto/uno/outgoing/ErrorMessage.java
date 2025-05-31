package com.backend.aitbackend.dto.uno.outgoing;

import com.backend.aitbackend.dto.uno.WebSocketMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorMessage extends WebSocketMessage {
    @JsonProperty("message")
    private String message;

    public ErrorMessage() {
        super("ERROR");
    }

    public ErrorMessage(String message) {
        this();
        this.message = message;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
