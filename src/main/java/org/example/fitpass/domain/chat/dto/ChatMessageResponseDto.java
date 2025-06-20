package org.example.fitpass.domain.chat.dto;

import java.time.LocalDateTime;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.enums.SenderType;

public record ChatMessageResponseDto(
    Long id,
    String content,
    SenderType senderType,
    LocalDateTime createdAt
){
    public static ChatMessageResponseDto from(ChatMessage entity) {
        return new ChatMessageResponseDto(
            entity.getId(),
            entity.getContent(),
            entity.getSenderType(),
            entity.getCreatedAt()
        );
    }
}

