package org.example.fitpass.domain.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns(
                "http://localhost:5173",
                "http://127.0.0.1:5500",
                "https://www.fitpass-13.com",
                "https://fitpass-13.com"
            )
            .withSockJS(); // SockJS 지원 추가
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트에서 서버로 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");
        
        // 서버에서 클라이언트로 메시지를 보낼 때 사용할 prefix
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 특정 사용자에게 메시지를 보낼 때 사용할 prefix
        registry.setUserDestinationPrefix("/user");
    }
}


