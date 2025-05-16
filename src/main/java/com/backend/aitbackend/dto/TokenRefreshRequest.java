package com.backend.aitbackend.dto;

public class TokenRefreshRequest {
    private String refreshToken;
    
    // Default constructor
    public TokenRefreshRequest() {
    }
    
    // Constructor with parameters
    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getters and setters
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
