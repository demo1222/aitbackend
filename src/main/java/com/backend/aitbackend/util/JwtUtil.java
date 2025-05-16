package com.backend.aitbackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Secret keys used for signing the tokens - should be externalized in production
    private final Key accessTokenKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final Key refreshTokenKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    
    @Value("${jwt.access.expiration:3600000}") // default 1 hour in milliseconds
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh.expiration:2592000000}") // default 30 days in milliseconds
    private long refreshTokenExpiration;

    // Generate access token for user
    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenExpiration, accessTokenKey);
    }
    
    // Generate refresh token for user
    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiration, refreshTokenKey);
    }
    
    private String generateToken(String username, long expiration, Key key) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
    
    // Validate access token
    public boolean validateAccessToken(String token) {
        return !isTokenExpired(token, accessTokenKey);
    }
    
    // Validate refresh token
    public boolean validateRefreshToken(String token) {
        return !isTokenExpired(token, refreshTokenKey);
    }
    
    // Extract username from token
    public String extractUsernameFromAccessToken(String token) {
        return extractClaim(token, Claims::getSubject, accessTokenKey);
    }
    
    public String extractUsernameFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, refreshTokenKey);
    }
    
    // Extract expiration date from token
    public Date extractExpirationFromAccessToken(String token) {
        return extractClaim(token, Claims::getExpiration, accessTokenKey);
    }
    
    public Date extractExpirationFromRefreshToken(String token) {
        return extractClaim(token, Claims::getExpiration, refreshTokenKey);
    }
    
    // Extract a specific claim from token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Key key) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }
    
    // Extract all claims from token
    private Claims extractAllClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // Check if token is expired
    private boolean isTokenExpired(String token, Key key) {
        final Date expiration = extractClaim(token, Claims::getExpiration, key);
        return expiration.before(new Date());
    }
}
