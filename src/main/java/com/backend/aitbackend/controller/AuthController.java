package com.backend.aitbackend.controller;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.aitbackend.dto.RegisterRequest;
import com.backend.aitbackend.dto.RegisterResponse;
import com.backend.aitbackend.model.User;
import com.backend.aitbackend.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
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
}
