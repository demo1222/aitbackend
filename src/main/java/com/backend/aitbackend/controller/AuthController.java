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
import com.backend.aitbackend.dto.RegisterRequest;
import com.backend.aitbackend.dto.TokenRefreshRequest;
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
            return ResponseEntity.badRequest().body(Map.of("message", "Username cannot be empty!"));
        }
        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email cannot be empty!"));
        }
        if (!EMAIL_PATTERN.matcher(registerRequest.getEmail()).matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid email format!"));
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password cannot be empty!"));
        }
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken!"));
        }
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already in use!"));
        }
        try {
            User registeredUser = userService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", registeredUser.getId(),
                "username", registeredUser.getUsername(),
                "email", registeredUser.getEmail(),
                "message", "User registered successfully!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username cannot be empty!"));
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password cannot be empty!"));
        }
        try {
            if (!userService.userExists(loginRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password!"));
            }
            if (userService.validateUserCredentials(loginRequest.getUsername(), loginRequest.getPassword())) {
                Optional<User> userOptional = userService.findByUsername(loginRequest.getUsername());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    String accessToken = userService.generateAccessToken(user.getUsername());
                    String refreshToken = userService.generateRefreshToken(user.getUsername());
                    return ResponseEntity.ok(Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "message", "Login successful!"
                    ));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Refresh token cannot be empty!"));
        }
        try {
            if (userService.validateRefreshToken(request.getRefreshToken())) {
                String username = userService.getUsernameFromRefreshToken(request.getRefreshToken());
                String newAccessToken = userService.generateAccessToken(username);
                String newRefreshToken = userService.generateRefreshToken(username);
                return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken,
                    "message", "Token refresh successful!"
                ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid refresh token!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Token refresh failed: " + e.getMessage()));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Token cannot be empty!"));
        }
        try {
            boolean isValid = userService.validateAccessToken(token);
            if (isValid) {
                String username = userService.getUsernameFromAccessToken(token);
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "message", "Token is valid!"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Token is invalid or expired!"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "valid", false,
                    "message", "Token validation failed: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String token = jwtAuthenticationUtil.getTokenFromRequest(request);
            if (token == null || !userService.validateAccessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or missing token"));
            }
            String username = userService.getUsernameFromAccessToken(token);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token: username not found"));
            }
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
            }
            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "virtualBalance", user.getVirtualBalance()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch user: " + e.getMessage()));
        }
    }
}
