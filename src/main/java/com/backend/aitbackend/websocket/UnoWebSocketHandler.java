package com.backend.aitbackend.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import com.backend.aitbackend.dto.uno.incoming.CallUnoMessage;
import com.backend.aitbackend.dto.uno.incoming.ChallengeUnoMessage;
import com.backend.aitbackend.dto.uno.incoming.CreateRoomMessage;
import com.backend.aitbackend.dto.uno.incoming.DrawCardMessage;
import com.backend.aitbackend.dto.uno.incoming.JoinRoomMessage;
import com.backend.aitbackend.dto.uno.incoming.LeaveRoomMessage;
import com.backend.aitbackend.dto.uno.incoming.PlayCardMessage;
import com.backend.aitbackend.dto.uno.incoming.StartGameMessage;
import com.backend.aitbackend.dto.uno.incoming.ToggleReadyMessage;
import com.backend.aitbackend.dto.uno.outgoing.CardPlayedMessage;
import com.backend.aitbackend.dto.uno.outgoing.CardsDrawnMessage;
import com.backend.aitbackend.dto.uno.outgoing.ErrorMessage;
import com.backend.aitbackend.dto.uno.outgoing.GameStartedMessage;
import com.backend.aitbackend.dto.uno.outgoing.GameStateMessage;
import com.backend.aitbackend.dto.uno.outgoing.PlayerJoinedMessage;
import com.backend.aitbackend.dto.uno.outgoing.PlayerLeftMessage;
import com.backend.aitbackend.dto.uno.outgoing.RoomCreatedMessage;
import com.backend.aitbackend.dto.uno.outgoing.RoomJoinedMessage;
import com.backend.aitbackend.dto.uno.outgoing.RoomUpdatedMessage;
import com.backend.aitbackend.dto.uno.outgoing.RoomsListMessage;
import com.backend.aitbackend.dto.uno.outgoing.UnoCalledMessage;
import com.backend.aitbackend.model.uno.Player;
import com.backend.aitbackend.model.uno.Room;
import com.backend.aitbackend.model.uno.UnoGameState;
import com.backend.aitbackend.service.UnoGameService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class UnoWebSocketHandler implements WebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UnoWebSocketHandler.class);
    
    private final UnoGameService gameService;
    private final ObjectMapper objectMapper;    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public UnoWebSocketHandler(UnoGameService gameService) {
        this.gameService = gameService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        logger.info("WebSocket connection established: {}", session.getId());
        
        // Send available rooms list to new connection
        sendRoomsList(session);
    }    @Override
    public void handleMessage(WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            handleTextMessage(session, (TextMessage) message);
        }
    }

    private void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            JsonNode jsonNode = objectMapper.readTree(payload);
            String messageType = jsonNode.get("type").asText();
            
            logger.info("Received message type: {} from session: {}", messageType, session.getId());
            
            switch (messageType) {
                case "GET_ROOMS":
                    handleGetRooms(session);
                    break;
                case "CREATE_ROOM":
                    handleCreateRoom(session, payload);
                    break;
                case "JOIN_ROOM":
                    handleJoinRoom(session, payload);
                    break;
                case "LEAVE_ROOM":
                    handleLeaveRoom(session, payload);
                    break;
                case "START_GAME":
                    handleStartGame(session, payload);
                    break;                case "TOGGLE_READY":
                    handleToggleReady(session, payload);
                    break;
                case "PLAY_CARD":
                    handlePlayCard(session, payload);
                    break;
                case "DRAW_CARD":
                    handleDrawCard(session, payload);
                    break;
                case "CALL_UNO":
                    handleCallUno(session, payload);
                    break;
                case "CHALLENGE_UNO":
                    handleChallengeUno(session, payload);
                    break;                case "GET_GAME_STATE":
                    handleGetGameState(session);
                    break;
                default:
                    sendError(session, "Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            logger.error("Error handling message from session {}: {}", session.getId(), e.getMessage(), e);
            sendError(session, "Failed to process message: " + e.getMessage());
        }
    }

    private void handleGetRooms(WebSocketSession session) {
        sendRoomsList(session);
    }

    private void handleCreateRoom(WebSocketSession session, String payload) throws IOException {
        CreateRoomMessage createMessage = objectMapper.readValue(payload, CreateRoomMessage.class);
        
        if (isInvalid(createMessage.getPlayerName()) || isInvalid(createMessage.getRoomName())) {
            sendError(session, "Player name and room name cannot be empty");
            return;
        }
        
        // Check if player is already in a room
        if (gameService.getPlayerBySession(session.getId()).isPresent()) {
            sendError(session, "You are already in a room");
            return;
        }
        
        try {
            Room room = gameService.createRoom(
                createMessage.getPlayerName().trim(),
                createMessage.getRoomName().trim(),
                createMessage.getMaxPlayers(),
                createMessage.isPrivate(),
                session.getId()
            );
            
            Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
            if (playerIdOpt.isPresent()) {
                sendMessage(session, new RoomCreatedMessage(room, playerIdOpt.get()));
                broadcastRoomsList();
                logger.info("Room created: {} by player: {}", room.getId(), createMessage.getPlayerName());
            }
        } catch (Exception e) {
            logger.error("Error creating room: {}", e.getMessage(), e);
            sendError(session, "Failed to create room: " + e.getMessage());
        }
    }

    private void handleJoinRoom(WebSocketSession session, String payload) throws IOException {
        JoinRoomMessage joinMessage = objectMapper.readValue(payload, JoinRoomMessage.class);
        
        if (isInvalid(joinMessage.getPlayerName()) || isInvalid(joinMessage.getRoomId())) {
            sendError(session, "Player name and room ID cannot be empty");
            return;
        }
        
        // Check if player is already in a room
        if (gameService.getPlayerBySession(session.getId()).isPresent()) {
            sendError(session, "You are already in a room");
            return;
        }
        
        try {
            Optional<Room> roomOpt = gameService.joinRoom(
                joinMessage.getRoomId().trim(),
                joinMessage.getPlayerName().trim(),
                session.getId()
            );
            
            if (roomOpt.isPresent()) {
                Room room = roomOpt.get();
                Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
                
                if (playerIdOpt.isPresent()) {
                    sendMessage(session, new RoomJoinedMessage(room, playerIdOpt.get()));
                    broadcastToRoom(room.getId(), new PlayerJoinedMessage(joinMessage.getPlayerName(), room), session.getId());
                    broadcastRoomUpdated(room);
                    broadcastRoomsList();
                    logger.info("Player {} joined room: {}", joinMessage.getPlayerName(), room.getId());
                }
            } else {
                sendError(session, "Unable to join room. Room may be full, not found, or game already started.");
            }
        } catch (Exception e) {
            logger.error("Error joining room: {}", e.getMessage(), e);
            sendError(session, "Failed to join room: " + e.getMessage());
        }
    }

    private void handleLeaveRoom(WebSocketSession session, String payload) throws IOException {
        LeaveRoomMessage leaveMessage = objectMapper.readValue(payload, LeaveRoomMessage.class);
        
        if (isInvalid(leaveMessage.getPlayerId())) {
            sendError(session, "Player ID cannot be empty");
            return;
        }
        
        try {
            Optional<Player> playerOpt = gameService.getPlayerBySession(session.getId());
            if (playerOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            Player player = playerOpt.get();
            Optional<Room> roomOpt = gameService.leaveRoom(player.getId());
            
            if (roomOpt.isPresent()) {
                Room room = roomOpt.get();
                broadcastToRoom(room.getId(), new PlayerLeftMessage(player.getName(), room), null);
                broadcastRoomUpdated(room);
                broadcastRoomsList();
                logger.info("Player {} left room: {}", player.getName(), room.getId());
            } else {
                // Room was empty and removed
                broadcastRoomsList();
                logger.info("Player {} left and room was removed", player.getName());
            }
        } catch (Exception e) {
            logger.error("Error leaving room: {}", e.getMessage(), e);
            sendError(session, "Failed to leave room: " + e.getMessage());
        }
    }

    private void handleStartGame(WebSocketSession session, String payload) throws IOException {
        StartGameMessage startMessage = objectMapper.readValue(payload, StartGameMessage.class);
        
        if (isInvalid(startMessage.getRoomId())) {
            sendError(session, "Room ID cannot be empty");
            return;
        }
        
        try {
            Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
            if (playerIdOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            String playerId = playerIdOpt.get();
            if (!gameService.validatePlayerInRoom(playerId, startMessage.getRoomId())) {
                sendError(session, "You are not in the specified room");
                return;
            }
            
            Optional<Room> roomOpt = gameService.getRoom(startMessage.getRoomId());
            if (roomOpt.isEmpty()) {
                sendError(session, "Room not found");
                return;
            }
            
            Room room = roomOpt.get();
            if (!room.isOwner(playerId)) {
                sendError(session, "Only room owner can start the game");
                return;
            }
            
            if (!room.canStartGame()) {
                sendError(session, "Cannot start game. Ensure all players are ready and minimum 2 players are present.");
                return;
            }
            
            if (gameService.startGame(startMessage.getRoomId(), playerId)) {
                broadcastToRoom(room.getId(), new GameStartedMessage(room.getId()), null);
                broadcastRoomUpdated(room);
                broadcastRoomsList();
                logger.info("Game started in room: {} by player: {}", room.getId(), playerId);
            } else {
                sendError(session, "Failed to start game");
            }
        } catch (Exception e) {
            logger.error("Error starting game: {}", e.getMessage(), e);
            sendError(session, "Failed to start game: " + e.getMessage());
        }
    }

    private void handleToggleReady(WebSocketSession session, String payload) throws IOException {
        ToggleReadyMessage toggleMessage = objectMapper.readValue(payload, ToggleReadyMessage.class);
        
        if (isInvalid(toggleMessage.getPlayerId())) {
            sendError(session, "Player ID cannot be empty");
            return;
        }
        
        try {
            Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
            if (playerIdOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            String playerId = playerIdOpt.get();
            if (!playerId.equals(toggleMessage.getPlayerId())) {
                sendError(session, "You can only toggle your own ready status");
                return;
            }
            
            if (gameService.togglePlayerReady(playerId)) {
                Optional<Room> roomOpt = gameService.getRoomByPlayer(playerId);
                if (roomOpt.isPresent()) {
                    Room room = roomOpt.get();
                    broadcastRoomUpdated(room);
                    logger.info("Player {} toggled ready status in room: {}", playerId, room.getId());
                }
            } else {
                sendError(session, "Failed to toggle ready status");
            }
        } catch (Exception e) {
            logger.error("Error toggling ready status: {}", e.getMessage(), e);
            sendError(session, "Failed to toggle ready status: " + e.getMessage());
        }    }

    // Game action handlers
    private void handlePlayCard(WebSocketSession session, String payload) throws IOException {
        PlayCardMessage playMessage = objectMapper.readValue(payload, PlayCardMessage.class);
        
        if (isInvalid(playMessage.getPlayerId()) || playMessage.getCard() == null) {
            sendError(session, "Player ID and card cannot be null");
            return;
        }
        
        try {
            Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
            if (playerIdOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            String playerId = playerIdOpt.get();
            if (!playerId.equals(playMessage.getPlayerId())) {
                sendError(session, "You can only play cards for yourself");
                return;
            }
            
            Optional<Room> roomOpt = gameService.getRoomByPlayer(playerId);
            if (roomOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            Room room = roomOpt.get();
            if (!room.hasActiveGame()) {
                sendError(session, "Game is not active");
                return;
            }
            
            UnoGameState gameState = room.getGameState();
            if (!gameState.getCurrentPlayerId().equals(playerId)) {
                sendError(session, "It's not your turn");
                return;
            }
            
            // Play the card
            UnoGameState.GamePlayResult result = gameService.playCard(room.getId(), playerId, playMessage.getCard(), playMessage.getDeclaredColor());
            
            if (result.isSuccess()) {                // Broadcast card played to all players in room
                CardPlayedMessage cardPlayedMsg = new CardPlayedMessage();
                cardPlayedMsg.setRoomId(room.getId());
                cardPlayedMsg.setPlayerId(playerId);
                cardPlayedMsg.setPlayerName(gameService.getPlayerBySession(session.getId()).get().getName());
                cardPlayedMsg.setPlayedCard(playMessage.getCard());
                cardPlayedMsg.setDeclaredColor(gameState.getDeclaredColor());
                cardPlayedMsg.setNextPlayerId(gameState.getCurrentPlayerId());
                broadcastToRoom(room.getId(), cardPlayedMsg, null);
                
                // Broadcast updated game state
                broadcastGameState(room);
                
                // Check if game is won
                if (!gameState.isGameActive()) {
                    broadcastGameFinished(room);
                }
                
                logger.info("Player {} played card {} in room {}", playerId, playMessage.getCard(), room.getId());
            } else {
                sendError(session, "Invalid card play: " + result.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error playing card: {}", e.getMessage(), e);
            sendError(session, "Failed to play card: " + e.getMessage());
        }
    }
    
    private void handleDrawCard(WebSocketSession session, String payload) throws IOException {
        DrawCardMessage drawMessage = objectMapper.readValue(payload, DrawCardMessage.class);
        
        if (isInvalid(drawMessage.getPlayerId())) {
            sendError(session, "Player ID cannot be empty");
            return;
        }
        
        try {
            Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
            if (playerIdOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            String playerId = playerIdOpt.get();
            if (!playerId.equals(drawMessage.getPlayerId())) {
                sendError(session, "You can only draw cards for yourself");
                return;
            }
            
            Optional<Room> roomOpt = gameService.getRoomByPlayer(playerId);
            if (roomOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            Room room = roomOpt.get();
            if (!room.hasActiveGame()) {
                sendError(session, "Game is not active");
                return;
            }
            
            UnoGameState gameState = room.getGameState();
            if (!gameState.getCurrentPlayerId().equals(playerId)) {
                sendError(session, "It's not your turn");
                return;
            }
            
            // Draw cards
            int cardCount = drawMessage.getCount() > 0 ? drawMessage.getCount() : 1;
            UnoGameState.DrawResult result = gameService.drawCards(room.getId(), playerId, cardCount);
            
            if (result.isSuccess()) {
                // Broadcast cards drawn to all players in room
                CardsDrawnMessage cardsDrawnMsg = new CardsDrawnMessage(
                    room.getId(),
                    playerId, 
                    gameService.getPlayerBySession(session.getId()).get().getName(),
                    cardCount,
                    gameState.getCurrentPlayerId()
                );
                broadcastToRoom(room.getId(), cardsDrawnMsg, null);
                
                // Broadcast updated game state
                broadcastGameState(room);
                
                logger.info("Player {} drew {} card(s) in room {}", playerId, cardCount, room.getId());
            } else {
                sendError(session, "Failed to draw cards: " + result.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error drawing cards: {}", e.getMessage(), e);
            sendError(session, "Failed to draw cards: " + e.getMessage());
        }
    }
    
    private void handleCallUno(WebSocketSession session, String payload) throws IOException {
        CallUnoMessage unoMessage = objectMapper.readValue(payload, CallUnoMessage.class);
        
        if (isInvalid(unoMessage.getPlayerId())) {
            sendError(session, "Player ID cannot be empty");
            return;
        }
        
        try {
            Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
            if (playerIdOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            String playerId = playerIdOpt.get();
            if (!playerId.equals(unoMessage.getPlayerId())) {
                sendError(session, "You can only call UNO for yourself");
                return;
            }
            
            Optional<Room> roomOpt = gameService.getRoomByPlayer(playerId);
            if (roomOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            Room room = roomOpt.get();
            if (!room.hasActiveGame()) {
                sendError(session, "Game is not active");
                return;
            }
            
            // Call UNO
            boolean success = gameService.callUno(room.getId(), playerId);
            
            if (success) {
                // Broadcast UNO called to all players in room
                UnoCalledMessage unoCalledMsg = new UnoCalledMessage(
                    room.getId(),
                    playerId, 
                    gameService.getPlayerBySession(session.getId()).get().getName(),
                    true
                );
                broadcastToRoom(room.getId(), unoCalledMsg, null);
                
                // Broadcast updated game state
                broadcastGameState(room);
                
                logger.info("Player {} called UNO in room {}", playerId, room.getId());
            } else {
                sendError(session, "Failed to call UNO - you don't have exactly one card");
            }
        } catch (Exception e) {
            logger.error("Error calling UNO: {}", e.getMessage(), e);
            sendError(session, "Failed to call UNO: " + e.getMessage());
        }
    }
    
    private void handleChallengeUno(WebSocketSession session, String payload) throws IOException {
        ChallengeUnoMessage challengeMessage = objectMapper.readValue(payload, ChallengeUnoMessage.class);
        
        if (isInvalid(challengeMessage.getChallengerId()) || isInvalid(challengeMessage.getChallengedId())) {
            sendError(session, "Challenger ID and target player ID cannot be empty");
            return;
        }
        
        try {
            Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
            if (playerIdOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            String playerId = playerIdOpt.get();
            if (!playerId.equals(challengeMessage.getChallengerId())) {
                sendError(session, "You can only issue challenges as yourself");
                return;
            }
            
            Optional<Room> roomOpt = gameService.getRoomByPlayer(playerId);
            if (roomOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            Room room = roomOpt.get();
            if (!room.hasActiveGame()) {
                sendError(session, "Game is not active");
                return;
            }
            
            // Challenge UNO
            boolean success = gameService.challengeUno(room.getId(), challengeMessage.getChallengerId(), challengeMessage.getChallengedId());
            
            if (success) {
                // Broadcast challenge result to all players in room
                // The target player should draw 2 cards as penalty
                CardsDrawnMessage penaltyMsg = new CardsDrawnMessage(
                    room.getId(),
                    challengeMessage.getChallengedId(), 
                    gameService.getPlayerById(challengeMessage.getChallengedId()).get().getName(),
                    2,
                    room.getGameState().getCurrentPlayerId()
                );
                broadcastToRoom(room.getId(), penaltyMsg, null);
                
                // Broadcast updated game state
                broadcastGameState(room);
                
                logger.info("Player {} successfully challenged {} for not calling UNO in room {}", 
                    challengeMessage.getChallengerId(), challengeMessage.getChallengedId(), room.getId());
            } else {
                sendError(session, "Challenge failed - target player either called UNO or doesn't have one card");
            }
        } catch (Exception e) {
            logger.error("Error challenging UNO: {}", e.getMessage(), e);
            sendError(session, "Failed to challenge UNO: " + e.getMessage());
        }
    }
    
    private void handleGetGameState(WebSocketSession session) throws IOException {
        try {
            Optional<String> playerIdOpt = gameService.getPlayerIdBySession(session.getId());
            if (playerIdOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            String playerId = playerIdOpt.get();
            Optional<Room> roomOpt = gameService.getRoomByPlayer(playerId);
            if (roomOpt.isEmpty()) {
                sendError(session, "You are not in any room");
                return;
            }
            
            Room room = roomOpt.get();
            if (!room.hasActiveGame()) {
                sendError(session, "Game is not active");
                return;
            }
            
            // Send current game state to requesting player
            sendGameState(session, room, playerId);
            
            logger.info("Sent game state to player {} in room {}", playerId, room.getId());
        } catch (Exception e) {
            logger.error("Error getting game state: {}", e.getMessage(), e);
            sendError(session, "Failed to get game state: " + e.getMessage());
        }
    }
    
    // Helper methods for game state broadcasting
    private void broadcastGameState(Room room) {
        room.getPlayers().forEach(player -> {
            WebSocketSession session = sessions.get(player.getSocketId());
            if (session != null && session.isOpen()) {
                sendGameState(session, room, player.getId());
            }
        });
    }    private void sendGameState(WebSocketSession session, Room room, String playerId) {
        try {
            UnoGameState gameState = room.getGameState();
            
            // Verify the player is in the room
            boolean playerInRoom = room.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(playerId));
                
            if (playerInRoom) {
                GameStateMessage stateMessage = new GameStateMessage(
                    room.getId(),
                    gameState.getTopCard(),
                    gameState.getDeclaredColor(),
                    gameState.getCurrentPlayerId(),
                    gameState.getDirection(),
                    gameState.getPlayerHandSizes(),
                    gameState.getCardsToDrawNext(),
                    gameState.isGameActive(),
                    gameState.getWinnerId(),
                    gameState.getDeckSize()
                );
                // Use the game state's player hand, not the player model's hand
                stateMessage.setYourHand(gameState.getPlayerHand(playerId));
                sendMessage(session, stateMessage);
            }
        } catch (Exception e) {
            logger.error("Error sending game state to player {}: {}", playerId, e.getMessage(), e);
        }
    }
    
    private void broadcastGameFinished(Room room) {
        UnoGameState gameState = room.getGameState();
        if (gameState.getWinnerId() != null) {
            // You can create a GameFinishedMessage DTO if needed
            // For now, we'll use the game state message
            broadcastGameState(room);
            logger.info("Game finished in room {} - Winner: {}", room.getId(), gameState.getWinnerId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("WebSocket connection closed: {} with status: {}", session.getId(), closeStatus);
        sessions.remove(session.getId());
        gameService.handleDisconnection(session.getId());
        broadcastRoomsList();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // Helper methods
    private void sendMessage(WebSocketSession session, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage(), e);
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        sendMessage(session, new ErrorMessage(errorMessage));
    }

    private void sendRoomsList(WebSocketSession session) {
        sendMessage(session, new RoomsListMessage(gameService.getAvailableRooms()));
    }

    private void broadcastRoomsList() {
        RoomsListMessage roomsMessage = new RoomsListMessage(gameService.getAvailableRooms());
        sessions.values().forEach(session -> sendMessage(session, roomsMessage));
    }

    private void broadcastRoomUpdated(Room room) {
        RoomUpdatedMessage updateMessage = new RoomUpdatedMessage(room);
        broadcastToRoom(room.getId(), updateMessage, null);
    }

    private void broadcastToRoom(String roomId, Object message, String excludeSessionId) {
        Optional<Room> roomOpt = gameService.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            return;
        }
        
        Room room = roomOpt.get();
        room.getPlayers().forEach(player -> {
            if (excludeSessionId == null || !player.getSocketId().equals(excludeSessionId)) {
                WebSocketSession session = sessions.get(player.getSocketId());
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                }
            }
        });
    }

    private boolean isInvalid(String value) {
        return value == null || value.trim().isEmpty();
    }
}
