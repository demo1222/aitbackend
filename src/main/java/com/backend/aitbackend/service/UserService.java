package com.backend.aitbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.aitbackend.config.SecurityConfig.PasswordEncoder;
import com.backend.aitbackend.model.User;
import com.backend.aitbackend.repository.UserRepository;
import com.backend.aitbackend.util.JwtUtil;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Transactional
    public User registerUser(String username, String email, String password) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        
        // Hash the password before storing it
        newUser.setPasswordHash(passwordEncoder.encode(password));
        
        // Virtual balance is already set to 10000 by default in the User class
        
        return userRepository.save(newUser);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
      @Transactional(readOnly = true)
    public boolean validateUserCredentials(String username, String password) {
        Optional<User> userOptional = findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return passwordEncoder.matches(password, user.getPasswordHash());
        }
        
        return false;
    }
    
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    
    public String generateAccessToken(String username) {
        return jwtUtil.generateAccessToken(username);
    }
    
    public String generateRefreshToken(String username) {
        return jwtUtil.generateRefreshToken(username);
    }
    
    public boolean validateAccessToken(String token) {
        return jwtUtil.validateAccessToken(token);
    }
    
    public boolean validateRefreshToken(String token) {
        return jwtUtil.validateRefreshToken(token);
    }
    
    public String getUsernameFromAccessToken(String token) {
        return jwtUtil.extractUsernameFromAccessToken(token);
    }
    
    public String getUsernameFromRefreshToken(String token) {
        return jwtUtil.extractUsernameFromRefreshToken(token);
    }
}