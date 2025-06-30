package org.example.fitpass.domain.chat.dto;

import java.time.LocalDateTime;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.enums.SenderType;

public record ChatMessageResponseDto(
    String type,
    Long id,
    Long senderId,
    SenderType senderType,
    String content,
    LocalDateTime createdAt
) {
    public static ChatMessageResponseDto from(ChatMessage entity) {
        return new ChatMessageResponseDto(
            "MESSAGE",
            entity.getId(),
            entity.getSenderType() == SenderType.USER
                ? entity.getChatRoom().getUser().getId()
                : entity.getChatRoom().getGym().getId(), // ← 이 부분에서 Gym이 맞는지 Trainer인지 확인 필요
            entity.getSenderType(),
            entity.getContent(),
            entity.getCreatedAt()
        );
    }
}