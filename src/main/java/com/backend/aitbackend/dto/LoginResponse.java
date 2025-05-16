package com.backend.aitbackend.dto;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String email;
    private String message;
    
    // Default constructor
    public LoginResponse() {
    }
    
    // Constructor with parameters
    public LoginResponse(String accessToken, String refreshToken, Long userId, String username, String email, String message) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
