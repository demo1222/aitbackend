package com.backend.aitbackend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.aitbackend.model.User;
import com.backend.aitbackend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Checking if username or email already exists
        if (userService.existsByUsername(user.getUsername())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (userService.existsByEmail(user.getEmail())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        
        // In a real application, you should hash the password here
        // This is just a placeholder for demonstration
        user.setPasswordHash(user.getPasswordHash()); 
        
        User savedUser = userService.saveUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        Optional<User> existingUser = userService.findById(id);
        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();
            
            // Update fields
            userToUpdate.setUsername(user.getUsername());
            userToUpdate.setEmail(user.getEmail());
            if (user.getPasswordHash() != null) {
                // In a real application, you would hash the password here
                userToUpdate.setPasswordHash(user.getPasswordHash());
            }
            if (user.getVirtualBalance() != null) {
                userToUpdate.setVirtualBalance(user.getVirtualBalance());
            }
            
            User updatedUser = userService.saveUser(userToUpdate);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}