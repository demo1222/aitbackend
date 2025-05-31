package com.backend.aitbackend.model.uno;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Room {
    private String id;
    private String name;
    private String owner;
    private List<Player> players;
    private int maxPlayers;    private boolean isPrivate;
    private boolean gameStarted;
    private LocalDateTime createdAt;
    private UnoGameState gameState;

    public Room() {
        this.players = new ArrayList<>();
        this.maxPlayers = 4;
        this.isPrivate = false;
        this.gameStarted = false;
        this.createdAt = LocalDateTime.now();
    }

    public Room(String id, String name, String owner, int maxPlayers, boolean isPrivate) {
        this();
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.maxPlayers = maxPlayers;
        this.isPrivate = isPrivate;
    }

    // Helper methods
    public boolean addPlayer(Player player) {
        if (players.size() >= maxPlayers) {
            return false;
        }
        if (players.stream().anyMatch(p -> p.getId().equals(player.getId()))) {
            return false;
        }
        players.add(player);
        return true;
    }

    public boolean removePlayer(String playerId) {
        boolean removed = players.removeIf(p -> p.getId().equals(playerId));
        
        // If owner left, assign new owner
        if (removed && owner.equals(playerId) && !players.isEmpty()) {
            owner = players.get(0).getId();
        }
        
        return removed;
    }

    public Optional<Player> getPlayer(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst();
    }

    public boolean isOwner(String playerId) {
        return owner.equals(playerId);
    }

    public boolean canStartGame() {
        return players.size() >= 2 && 
               players.stream().allMatch(player -> player.isReady() || player.getId().equals(owner)) &&
               !gameStarted;
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public boolean isEmpty() {
        return players.isEmpty();
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
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

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UnoGameState getGameState() {
        return gameState;
    }

    public void setGameState(UnoGameState gameState) {
        this.gameState = gameState;
    }

    public void startUnoGame() {
        if (canStartGame()) {
            this.gameState = new UnoGameState();
            List<String> playerIds = players.stream().map(Player::getId).toList();
            this.gameState.initializeGame(playerIds);
            this.gameStarted = true;
        }
    }

    public boolean hasActiveGame() {
        return gameStarted && gameState != null && gameState.isGameActive();
    }
}
