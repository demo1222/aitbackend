package com.backend.aitbackend.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig {

    /**
     * Creates a PasswordEncoder bean that uses SHA-256 for password hashing.
     * This is a simple implementation for demonstration purposes.
     * In a production environment, consider using stronger algorithms and libraries.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder();
    }

    /**
     * Configures CORS to allow all origins, methods, and headers.
     * 
     * @return a WebMvcConfigurer bean with the CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("localhost:4000", "http://localhost:4000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }

    /**
     * Simple PasswordEncoder class that uses SHA-256 for hashing.
     * This is for demonstration only and not suitable for production use.
     */
    public static class PasswordEncoder {
        
        /**
         * Encodes a password using SHA-256.
         * 
         * @param rawPassword the raw password to encode
         * @return the encoded password
         */
        public String encode(String rawPassword) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashedPassword = md.digest(rawPassword.getBytes());
                return Base64.getEncoder().encodeToString(hashedPassword);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to hash password", e);
            }
        }
        
        /**
         * Checks if a raw password matches an encoded password.
         * 
         * @param rawPassword the raw password to check
         * @param encodedPassword the encoded password to check against
         * @return true if the passwords match, false otherwise
         */
        public boolean matches(String rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    }
}
