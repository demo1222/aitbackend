package com.backend.aitbackend.dto.uno.outgoing;

import com.backend.aitbackend.model.uno.UnoCard;

public class CardPlayedMessage {
    private String type = "CARD_PLAYED";
    private String roomId;
    private String playerId;
    private String playerName;
    private UnoCard playedCard;
    private UnoCard.Color declaredColor;
    private String nextPlayerId;
    private boolean skipNextPlayer;
    private boolean directionReversed;
    private int cardsToDrawNext;
    private boolean playerHasUno;
    private boolean gameWon;
    private String winnerId;

    public CardPlayedMessage() {}

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public UnoCard getPlayedCard() {
        return playedCard;
    }

    public void setPlayedCard(UnoCard playedCard) {
        this.playedCard = playedCard;
    }

    public UnoCard.Color getDeclaredColor() {
        return declaredColor;
    }

    public void setDeclaredColor(UnoCard.Color declaredColor) {
        this.declaredColor = declaredColor;
    }

    public String getNextPlayerId() {
        return nextPlayerId;
    }

    public void setNextPlayerId(String nextPlayerId) {
        this.nextPlayerId = nextPlayerId;
    }

    public boolean isSkipNextPlayer() {
        return skipNextPlayer;
    }

    public void setSkipNextPlayer(boolean skipNextPlayer) {
        this.skipNextPlayer = skipNextPlayer;
    }

    public boolean isDirectionReversed() {
        return directionReversed;
    }

    public void setDirectionReversed(boolean directionReversed) {
        this.directionReversed = directionReversed;
    }

    public int getCardsToDrawNext() {
        return cardsToDrawNext;
    }

    public void setCardsToDrawNext(int cardsToDrawNext) {
        this.cardsToDrawNext = cardsToDrawNext;
    }

    public boolean isPlayerHasUno() {
        return playerHasUno;
    }

    public void setPlayerHasUno(boolean playerHasUno) {
        this.playerHasUno = playerHasUno;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }
}
