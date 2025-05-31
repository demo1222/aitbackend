package com.backend.aitbackend.dto.uno.incoming;

public class ChallengeUnoMessage {
    private String type = "CHALLENGE_UNO";
    private String challengerId;
    private String challengedId;
    private String roomId;

    public ChallengeUnoMessage() {}

    public ChallengeUnoMessage(String challengerId, String challengedId, String roomId) {
        this.challengerId = challengerId;
        this.challengedId = challengedId;
        this.roomId = roomId;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(String challengerId) {
        this.challengerId = challengerId;
    }

    public String getChallengedId() {
        return challengedId;
    }

    public void setChallengedId(String challengedId) {
        this.challengedId = challengedId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
