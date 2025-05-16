package com.backend.aitbackend.dto;

public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    private String message;
    
    // Default constructor
    public TokenRefreshResponse() {
    }
    
    // Constructor with parameters
    public TokenRefreshResponse(String accessToken, String refreshToken, String message) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
    }
    
    // Getters and setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
