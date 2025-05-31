package com.backend.aitbackend.model.uno;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UnoGameStateTest {
    
    private UnoGameState gameState;
    private List<String> playerIds;

    @BeforeEach
    void setUp() {
        gameState = new UnoGameState();
        playerIds = Arrays.asList("player1", "player2", "player3");
        gameState.initializeGame(playerIds);
    }    @Test
    void testSkipCardSkipsNextPlayer() {
        // Arrange
        String currentPlayerId = gameState.getCurrentPlayerId();
        UnoCard topCard = gameState.getTopCard();
        
        // Create a skip card that matches the top card's color
        UnoCard skipCard = new UnoCard(topCard.getColor(), UnoCard.Type.SKIP);
        // Add the skip card to current player's hand for testing
        gameState.addCardToPlayerHand(currentPlayerId, skipCard);
        
        // Get the player that should be skipped
        String nextPlayerId = getNextPlayerId(currentPlayerId);
        String playerAfterSkipped = getNextPlayerId(nextPlayerId);
        
        // Act
        UnoGameState.GamePlayResult result = gameState.playCard(currentPlayerId, skipCard, null);
        
        // Assert
        assertTrue(result.isSuccess(), "Skip card should be playable");
        assertTrue(result.isSkipNextPlayer(), "Result should indicate next player was skipped");
        assertEquals(playerAfterSkipped, gameState.getCurrentPlayerId(), 
                    "Current player should be the one after the skipped player");
    }    @Test
    void testDrawTwoCardMakesNextPlayerDrawAndSkipsTurn() {
        // Arrange
        String currentPlayerId = gameState.getCurrentPlayerId();
        UnoCard topCard = gameState.getTopCard();
        
        // Create a draw two card that matches the top card's color
        UnoCard drawTwoCard = new UnoCard(topCard.getColor(), UnoCard.Type.DRAW_TWO);
        // Add the draw two card to current player's hand for testing
        gameState.addCardToPlayerHand(currentPlayerId, drawTwoCard);
        
        // Get initial hand sizes
        String nextPlayerId = getNextPlayerId(currentPlayerId);
        String playerAfterNext = getNextPlayerId(nextPlayerId);
        int initialHandSize = gameState.getPlayerHandSize(nextPlayerId);
        
        // Act
        UnoGameState.GamePlayResult result = gameState.playCard(currentPlayerId, drawTwoCard, null);
        
        // Assert
        assertTrue(result.isSuccess(), "Draw two card should be playable");
        assertEquals(2, result.getCardsToDrawNext(), "Result should indicate 2 cards were drawn");
        assertEquals(initialHandSize + 2, gameState.getPlayerHandSize(nextPlayerId), 
                    "Next player should have 2 more cards");
        assertEquals(playerAfterNext, gameState.getCurrentPlayerId(), 
                    "Current player should be the one after the player who drew cards");
        assertEquals(0, gameState.getCardsToDrawNext(), 
                    "Game state should not have pending cards to draw");
    }    @Test
    void testWildDrawFourCardMakesNextPlayerDrawAndSkipsTurn() {
        // Arrange
        String currentPlayerId = gameState.getCurrentPlayerId();
        // Wild cards can always be played regardless of top card
        UnoCard wildDrawFourCard = new UnoCard(UnoCard.Color.WILD, UnoCard.Type.WILD_DRAW_FOUR);
        // Add the wild draw four card to current player's hand for testing
        gameState.addCardToPlayerHand(currentPlayerId, wildDrawFourCard);
        
        // Get initial hand sizes
        String nextPlayerId = getNextPlayerId(currentPlayerId);
        String playerAfterNext = getNextPlayerId(nextPlayerId);
        int initialHandSize = gameState.getPlayerHandSize(nextPlayerId);
        
        // Act
        UnoGameState.GamePlayResult result = gameState.playCard(currentPlayerId, wildDrawFourCard, UnoCard.Color.BLUE);
        
        // Assert
        assertTrue(result.isSuccess(), "Wild draw four card should be playable");
        assertEquals(4, result.getCardsToDrawNext(), "Result should indicate 4 cards were drawn");
        assertEquals(initialHandSize + 4, gameState.getPlayerHandSize(nextPlayerId), 
                    "Next player should have 4 more cards");
        assertEquals(playerAfterNext, gameState.getCurrentPlayerId(), 
                    "Current player should be the one after the player who drew cards");
        assertEquals(UnoCard.Color.BLUE, gameState.getDeclaredColor(), 
                    "Declared color should be blue");
        assertEquals(0, gameState.getCardsToDrawNext(), 
                    "Game state should not have pending cards to draw");
    }    @Test
    void testReverseCardInTwoPlayerGameActsLikeSkip() {
        // Arrange - Create a 2-player game
        UnoGameState twoPlayerGame = new UnoGameState();
        List<String> twoPlayerIds = Arrays.asList("player1", "player2");
        twoPlayerGame.initializeGame(twoPlayerIds);
        
        String currentPlayerId = twoPlayerGame.getCurrentPlayerId();
        UnoCard topCard = twoPlayerGame.getTopCard();
        
        // Create a reverse card that matches the top card's color
        UnoCard reverseCard = new UnoCard(topCard.getColor(), UnoCard.Type.REVERSE);
        // Add the reverse card to current player's hand for testing
        twoPlayerGame.addCardToPlayerHand(currentPlayerId, reverseCard);
        
        // In a 2-player game, after reverse the same player should play again
        
        // Act
        UnoGameState.GamePlayResult result = twoPlayerGame.playCard(currentPlayerId, reverseCard, null);
        
        // Assert
        assertTrue(result.isSuccess(), "Reverse card should be playable");
        assertTrue(result.isSkipNextPlayer(), "In 2-player game, reverse should act like skip");
        assertEquals(currentPlayerId, twoPlayerGame.getCurrentPlayerId(), 
                    "In 2-player game, reverse should return turn to same player");
    }    @Test
    void testReverseCardInMultiPlayerGameChangesDirection() {
        // Arrange
        String currentPlayerId = gameState.getCurrentPlayerId();
        UnoCard topCard = gameState.getTopCard();
        
        // Create a reverse card that matches the top card's color
        UnoCard reverseCard = new UnoCard(topCard.getColor(), UnoCard.Type.REVERSE);
        // Add the reverse card to current player's hand for testing
        gameState.addCardToPlayerHand(currentPlayerId, reverseCard);
        
        UnoGameState.Direction initialDirection = gameState.getDirection();
        
        // Act
        UnoGameState.GamePlayResult result = gameState.playCard(currentPlayerId, reverseCard, null);
        
        // Assert
        assertTrue(result.isSuccess(), "Reverse card should be playable");
        assertTrue(result.isDirectionReversed(), "Result should indicate direction was reversed");
        assertNotEquals(initialDirection, gameState.getDirection(), 
                       "Direction should have changed");
    }

    private String getNextPlayerId(String currentPlayerId) {
        List<String> playerOrder = gameState.getPlayerOrder();
        int currentIndex = playerOrder.indexOf(currentPlayerId);
        
        if (gameState.getDirection() == UnoGameState.Direction.CLOCKWISE) {
            return playerOrder.get((currentIndex + 1) % playerOrder.size());
        } else {
            return playerOrder.get((currentIndex - 1 + playerOrder.size()) % playerOrder.size());
        }
    }
}
