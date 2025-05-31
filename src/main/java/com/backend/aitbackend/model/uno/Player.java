package com.backend.aitbackend.model.uno;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private String id;
    private String name;
    private int cardCount;
    private boolean isReady;
    private String socketId;
    private LocalDateTime lastActivity;
    private List<UnoCard> hand;
    private boolean saidUno;    public Player() {
        this.hand = new ArrayList<>();
        this.saidUno = false;
    }

    public Player(String id, String name, String socketId) {
        this();
        this.id = id;
        this.name = name;
        this.socketId = socketId;
        this.cardCount = 0;
        this.isReady = false;
        this.lastActivity = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    public List<UnoCard> getHand() {
        return hand;
    }

    public void setHand(List<UnoCard> hand) {
        this.hand = hand;
        this.cardCount = hand.size();
    }

    public boolean isSaidUno() {
        return saidUno;
    }

    public void setSaidUno(boolean saidUno) {
        this.saidUno = saidUno;
    }

    public void addCard(UnoCard card) {
        hand.add(card);
        this.cardCount = hand.size();
    }

    public void addCards(List<UnoCard> cards) {
        hand.addAll(cards);
        this.cardCount = hand.size();
    }

    public boolean removeCard(UnoCard card) {
        boolean removed = hand.remove(card);
        if (removed) {
            this.cardCount = hand.size();
        }
        return removed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
