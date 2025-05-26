package com.backend.aitbackend.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtAuthenticationUtil {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtUtil jwtUtil;
    
    public JwtAuthenticationUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Extract JWT token from the Authorization header
     * 
     * @param request The HTTP request
     * @return The JWT token or null if not found
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
    
    /**
     * Validate the access token from the request
     * 
     * @param request The HTTP request
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return token != null && jwtUtil.validateAccessToken(token);
    }
    
    /**
     * Get username from the access token in the request
     * 
     * @param request The HTTP request
     * @return The username or null if token is invalid
     */
    public String getUsernameFromToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null && jwtUtil.validateAccessToken(token)) {
            return jwtUtil.extractUsernameFromAccessToken(token);
        }
        return null;
    }
}
