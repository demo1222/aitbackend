package com.backend.aitbackend.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.backend.aitbackend.model.uno.Player;
import com.backend.aitbackend.model.uno.Room;
import com.backend.aitbackend.model.uno.UnoCard;
import com.backend.aitbackend.model.uno.UnoGameState;

@Service
public class UnoGameService {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> playerToRoom = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    public String generateRoomId() {
        return "ROOM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public String generatePlayerId() {
        return "PLAYER_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Room createRoom(String playerName, String roomName, int maxPlayers, boolean isPrivate, String sessionId) {
        String roomId = generateRoomId();
        String playerId = generatePlayerId();
        
        Player owner = new Player(playerId, playerName, sessionId);
        Room room = new Room(roomId, roomName, playerId, maxPlayers, isPrivate);
        room.addPlayer(owner);
        
        rooms.put(roomId, room);
        playerToRoom.put(playerId, roomId);
        sessionToPlayer.put(sessionId, playerId);
        
        return room;
    }

    public Optional<Room> joinRoom(String roomId, String playerName, String sessionId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Optional.empty();
        }
        
        if (room.isFull()) {
            return Optional.empty();
        }
        
        if (room.isGameStarted()) {
            return Optional.empty();
        }
        
        String playerId = generatePlayerId();
        Player player = new Player(playerId, playerName, sessionId);
        
        if (room.addPlayer(player)) {
            playerToRoom.put(playerId, roomId);
            sessionToPlayer.put(sessionId, playerId);
            return Optional.of(room);
        }
        
        return Optional.empty();
    }

    public Optional<Room> leaveRoom(String playerId) {
        String roomId = playerToRoom.get(playerId);
        if (roomId == null) {
            return Optional.empty();
        }
        
        Room room = rooms.get(roomId);
        if (room == null) {
            return Optional.empty();
        }
        
        Optional<Player> playerOpt = room.getPlayer(playerId);
        if (playerOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Player player = playerOpt.get();
        String sessionId = player.getSocketId();
        
        room.removePlayer(playerId);
        playerToRoom.remove(playerId);
        sessionToPlayer.remove(sessionId);
        
        // Clean up empty room
        if (room.isEmpty()) {
            rooms.remove(roomId);
            return Optional.empty();
        }
        
        return Optional.of(room);
    }

    public boolean togglePlayerReady(String playerId) {
        String roomId = playerToRoom.get(playerId);
        if (roomId == null) {
            return false;
        }
        
        Room room = rooms.get(roomId);
        if (room == null) {
            return false;
        }
        
        Optional<Player> playerOpt = room.getPlayer(playerId);
        if (playerOpt.isEmpty()) {
            return false;
        }
        
        Player player = playerOpt.get();
        player.setReady(!player.isReady());
        player.updateLastActivity();
        
        return true;
    }    public boolean startGame(String roomId, String playerId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return false;
        }
        
        if (!room.isOwner(playerId)) {
            return false;
        }
        
        if (!room.canStartGame()) {
            return false;
        }
        
        room.startUnoGame();
        return true;
    }

    public List<Room> getAvailableRooms() {
        return rooms.values().stream()
                .filter(room -> !room.isPrivate() && !room.isGameStarted() && !room.isFull())
                .collect(Collectors.toList());
    }

    public Optional<Room> getRoomByPlayer(String playerId) {
        String roomId = playerToRoom.get(playerId);
        if (roomId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rooms.get(roomId));
    }

    public Optional<String> getPlayerIdBySession(String sessionId) {
        return Optional.ofNullable(sessionToPlayer.get(sessionId));
    }

    public Optional<Player> getPlayerBySession(String sessionId) {
        String playerId = sessionToPlayer.get(sessionId);
        if (playerId == null) {
            return Optional.empty();
        }
        
        String roomId = playerToRoom.get(playerId);
        if (roomId == null) {
            return Optional.empty();
        }
        
        Room room = rooms.get(roomId);
        if (room == null) {
            return Optional.empty();
        }
        
        return room.getPlayer(playerId);
    }

    public void handleDisconnection(String sessionId) {
        String playerId = sessionToPlayer.get(sessionId);
        if (playerId != null) {
            leaveRoom(playerId);
        }
    }

    public void cleanupInactiveRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        
        List<String> roomsToRemove = rooms.values().stream()
                .filter(room -> room.getCreatedAt().isBefore(threshold) && 
                               room.getPlayers().stream().allMatch(p -> p.getLastActivity().isBefore(threshold)))
                .map(Room::getId)
                .collect(Collectors.toList());
        
        roomsToRemove.forEach(roomId -> {
            Room room = rooms.remove(roomId);
            if (room != null) {
                room.getPlayers().forEach(player -> {
                    playerToRoom.remove(player.getId());
                    sessionToPlayer.remove(player.getSocketId());
                });
            }
        });
    }

    public Optional<Room> getRoom(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }    public boolean validatePlayerInRoom(String playerId, String roomId) {
        String actualRoomId = playerToRoom.get(playerId);
        return roomId.equals(actualRoomId);
    }

    // Game action methods
    public UnoGameState.GamePlayResult playCard(String roomId, String playerId, UnoCard card, UnoCard.Color declaredColor) {
        Room room = rooms.get(roomId);
        if (room == null || !room.hasActiveGame()) {
            return new UnoGameState.GamePlayResult(false, "Game not active");
        }
        
        if (!validatePlayerInRoom(playerId, roomId)) {
            return new UnoGameState.GamePlayResult(false, "Player not in room");
        }
        
        return room.getGameState().playCard(playerId, card, declaredColor);
    }

    public UnoGameState.DrawResult drawCards(String roomId, String playerId, int count) {
        Room room = rooms.get(roomId);
        if (room == null || !room.hasActiveGame()) {
            return new UnoGameState.DrawResult(false, "Game not active", Collections.emptyList());
        }
        
        if (!validatePlayerInRoom(playerId, roomId)) {
            return new UnoGameState.DrawResult(false, "Player not in room", Collections.emptyList());
        }
        
        return room.getGameState().drawCards(playerId, count);
    }

    public boolean callUno(String roomId, String playerId) {
        Room room = rooms.get(roomId);
        if (room == null || !room.hasActiveGame()) {
            return false;
        }
        
        if (!validatePlayerInRoom(playerId, roomId)) {
            return false;
        }
        
        return room.getGameState().callUno(playerId);
    }

    public boolean challengeUno(String roomId, String challengerId, String challengedId) {
        Room room = rooms.get(roomId);
        if (room == null || !room.hasActiveGame()) {
            return false;
        }
        
        if (!validatePlayerInRoom(challengerId, roomId) || !validatePlayerInRoom(challengedId, roomId)) {
            return false;
        }
        
        return room.getGameState().challengeUno(challengerId, challengedId);
    }

    public UnoGameState getGameState(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return null;
        }
        return room.getGameState();
    }

    public Optional<Player> getPlayerById(String playerId) {
        String roomId = playerToRoom.get(playerId);
        if (roomId == null) {
            return Optional.empty();
        }
        
        Room room = rooms.get(roomId);
        if (room == null) {
            return Optional.empty();
        }
        
        return room.getPlayer(playerId);
    }

    public List<UnoCard> getPlayerHand(String roomId, String playerId) {
        Room room = rooms.get(roomId);
        if (room == null || !room.hasActiveGame()) {
            return Collections.emptyList();
        }
        
        if (!validatePlayerInRoom(playerId, roomId)) {
            return Collections.emptyList();
        }
        
        return room.getGameState().getPlayerHand(playerId);
    }
}
