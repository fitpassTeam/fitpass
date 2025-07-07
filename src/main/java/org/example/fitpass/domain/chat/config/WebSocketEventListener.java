package org.example.fitpass.domain.chat.config;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
import org.example.fitpass.domain.chat.enums.SenderType;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("새로운 웹소켓 연결이 설정되었습니다.");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String userType = (String) headerAccessor.getSessionAttributes().get("userType");

        if (userId != null) {
            log.info("사용자 {} 퇴장", userId);

            ChatMessageResponseDto chatMessage = new ChatMessageResponseDto(
                "LEAVE",
                null, // id
                Long.parseLong(userId),
                SenderType.valueOf(userType),
                "채팅방을 나갔습니다.",
                LocalDateTime.now() // 또는 null
            );

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}