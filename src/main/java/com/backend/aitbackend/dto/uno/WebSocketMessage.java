package com.backend.aitbackend.dto.uno;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebSocketMessage {
    @JsonProperty("type")
    private String type;

    public WebSocketMessage() {}

    public WebSocketMessage(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
