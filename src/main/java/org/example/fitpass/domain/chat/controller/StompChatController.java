package org.example.fitpass.domain.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.domain.chat.dto.ChatMessageRequestDto;
import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.enums.SenderType;
import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StompChatController {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageResponseDto sendMessage(@Payload ChatMessageRequestDto chatMessageRequest) {
        try {
            Long senderId = chatMessageRequest.senderId();
            Long receiverId = chatMessageRequest.receiverId();
            String content = chatMessageRequest.message();
            SenderType senderType = chatMessageRequest.senderType();

            User user = userRepository.findById(
                    senderType == SenderType.USER ? senderId : receiverId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Gym gym = gymRepository.findById(
                    senderType == SenderType.USER ? receiverId : senderId)
                .orElseThrow(() -> new EntityNotFoundException("Gym not found"));

            ChatRoom room = chatRoomRepository.findByUserAndGym(
                    senderType == SenderType.USER ? senderId : receiverId,
                    senderType == SenderType.USER ? receiverId : senderId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.of(user, gym);
                    return chatRoomRepository.save(newRoom);
                });

            ChatMessage chatMessage = ChatMessage.of(room, content, senderType);
            chatMessage = chatMessageRepository.save(chatMessage);

            ChatMessageResponseDto responseDto = ChatMessageResponseDto.from(chatMessage);

            String receiverDestination = "/user/" + receiverId + "/queue/messages";
            messagingTemplate.convertAndSend(receiverDestination, responseDto);

            return responseDto;

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("메시지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageResponseDto addUser(@Payload ChatMessageRequestDto chatMessageRequest,
        SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("userId", chatMessageRequest.senderId());
        headerAccessor.getSessionAttributes().put("userType", chatMessageRequest.senderType());

        log.info("사용자 {} 접속", chatMessageRequest.senderId());

        return new ChatMessageResponseDto(
            "JOIN",
            null,
            chatMessageRequest.senderId(),
            chatMessageRequest.senderType(),
            "채팅방에 입장했습니다.",
            null
        );
    }

    @MessageMapping("/chat.leaveUser")
    @SendTo("/topic/public")
    public ChatMessageResponseDto leaveUser(@Payload ChatMessageRequestDto chatMessageRequest) {
        log.info("사용자 {} 퇴장", chatMessageRequest.senderId());

        return new ChatMessageResponseDto(
            "LEAVE",
            null,
            chatMessageRequest.senderId(),
            chatMessageRequest.senderType(),
            "채팅방을 나갔습니다.",
            null
        );
    }
}