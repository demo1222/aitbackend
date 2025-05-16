package com.backend.aitbackend.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.aitbackend.util.JwtAuthenticationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationUtil jwtAuthUtil;
    private final ObjectMapper objectMapper;
    
    public JwtAuthenticationFilter(JwtAuthenticationUtil jwtAuthUtil, ObjectMapper objectMapper) {
        this.jwtAuthUtil = jwtAuthUtil;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Define paths that don't need authentication
        String path = request.getServletPath();
        return path.startsWith("/auth/") || path.equals("/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        // Skip authentication for certain paths
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check if token is valid
        if (!jwtAuthUtil.validateToken(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            
            objectMapper.writeValue(response.getWriter(), 
                    Map.of("error", "Unauthorized", "message", "Invalid or missing authentication token"));
            return;
        }
        
        // Continue with the filter chain if token is valid
        filterChain.doFilter(request, response);
    }
}
