package com.backend.aitbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import com.backend.aitbackend.websocket.EchoWebSocketHandler;
import com.backend.aitbackend.websocket.UnoWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private EchoWebSocketHandler echoWebSocketHandler;
    
    @Autowired
    private UnoWebSocketHandler unoWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Original echo handler
        registry.addHandler(echoWebSocketHandler, "/ws")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
        
        // UNO game WebSocket handler on port 8080 endpoint
        registry.addHandler(unoWebSocketHandler, "/uno")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
