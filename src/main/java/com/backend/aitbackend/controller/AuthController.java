package com.backend.aitbackend.controller;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.aitbackend.dto.LoginRequest;
import com.backend.aitbackend.dto.LoginResponse;
import com.backend.aitbackend.dto.RegisterRequest;
import com.backend.aitbackend.dto.RegisterResponse;
import com.backend.aitbackend.dto.TokenRefreshRequest;
import com.backend.aitbackend.dto.TokenRefreshResponse;
import com.backend.aitbackend.model.User;
import com.backend.aitbackend.service.UserService;
import com.backend.aitbackend.util.JwtAuthenticationUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private final UserService userService;
    private final JwtAuthenticationUtil jwtAuthenticationUtil;
    
    public AuthController(UserService userService, JwtAuthenticationUtil jwtAuthenticationUtil) {
        this.userService = userService;
        this.jwtAuthenticationUtil = jwtAuthenticationUtil;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new RegisterResponse(null, null, null, "Username cannot be empty!"));
        }
        
        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new RegisterResponse(null, null, null, "Email cannot be empty!"));
        }
        
        // Validate email format
        if (!EMAIL_PATTERN.matcher(registerRequest.getEmail()).matches()) {
            return ResponseEntity
                    .badRequest()
                    .body(new RegisterResponse(null, null, null, "Invalid email format!"));
        }
        
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new RegisterResponse(null, null, null, "Password cannot be empty!"));
        }
        
        
        // Check if username already exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new RegisterResponse(null, null, null, "Username is already taken!"));
        }
        
        // Check if email already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new RegisterResponse(null, null, null, "Email is already in use!"));
        }
        
        try {
            // Create new user
            User registeredUser = userService.registerUser(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword());
            
            // Return successful response
            RegisterResponse response = new RegisterResponse(
                    registeredUser.getId(),
                    registeredUser.getUsername(),
                    registeredUser.getEmail(),
                    "User registered successfully!");
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RegisterResponse(null, null, null, "Registration failed: " + e.getMessage()));
        }
    }
      @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        // Validate request
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse(null, null, null, null, null, "Username cannot be empty!"));
        }
        
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse(null, null, null, null, null, "Password cannot be empty!"));
        }
        
        try {
            // Check if user exists
            if (!userService.userExists(loginRequest.getUsername())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(null, null, null, null, null, "Invalid username or password!"));
            }
            
            // Validate user credentials
            if (userService.validateUserCredentials(loginRequest.getUsername(), loginRequest.getPassword())) {
                // Get user details
                Optional<User> userOptional = userService.findByUsername(loginRequest.getUsername());
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    
                    // Generate tokens
                    String accessToken = userService.generateAccessToken(user.getUsername());
                    String refreshToken = userService.generateRefreshToken(user.getUsername());
                    
                    // Return successful response
                    LoginResponse response = new LoginResponse(
                            accessToken,
                            refreshToken,
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            "Login successful!");
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            // If we reach here, login failed
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(null, null, null, null, null, "Invalid username or password!"));
            
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(null, null, null, null, null, "Login failed: " + e.getMessage()));
        }
    }
      @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        // Validate request
        if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new TokenRefreshResponse(null, null, "Refresh token cannot be empty!"));
        }
        
        try {
            // Validate refresh token
            if (userService.validateRefreshToken(request.getRefreshToken())) {
                // Extract username from the refresh token
                String username = userService.getUsernameFromRefreshToken(request.getRefreshToken());
                
                // Generate new tokens
                String newAccessToken = userService.generateAccessToken(username);
                String newRefreshToken = userService.generateRefreshToken(username);
                
                // Return successful response
                return ResponseEntity.ok(new TokenRefreshResponse(
                        newAccessToken,
                        newRefreshToken,
                        "Token refresh successful!"));
            }
            
            // If we reach here, token refresh failed
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenRefreshResponse(null, null, "Invalid refresh token!"));
            
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TokenRefreshResponse(null, null, "Token refresh failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        // Validate request
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("valid", false, "message", "Token cannot be empty!"));
        }
        
        try {
            // Validate access token
            boolean isValid = userService.validateAccessToken(token);
            
            if (isValid) {
                String username = userService.getUsernameFromAccessToken(token);
                return ResponseEntity.ok(Map.of(
                        "valid", true, 
                        "username", username, 
                        "message", "Token is valid!"));
            } else {
                return ResponseEntity.ok(Map.of(
                        "valid", false, 
                        "message", "Token is invalid or expired!"));
            }
            
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "valid", false, 
                            "message", "Token validation failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            // Extract and validate token
            String token = jwtAuthenticationUtil.getTokenFromRequest(request);
            if (token == null || !userService.validateAccessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", 401, "message", "Invalid or missing token"));
            }
            String username = userService.getUsernameFromAccessToken(token);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", 401, "message", "Invalid token: username not found"));
            }
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", 404, "message", "User not found"));
            }
            User user = userOpt.get();
            // Only return safe fields
            Map<String, Object> result = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "virtualBalance", user.getVirtualBalance()
            );
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "result", result,
                "message", "User fetched successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 500, "message", "Failed to fetch user: " + e.getMessage()));
        }
    }
}
