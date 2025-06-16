package org.example.fitpass.domain.chat.dto;

import java.time.LocalDateTime;
import org.example.fitpass.domain.chat.enums.SenderType;

public record ChatMessageDto(
    Long messageId,
    Long id,
    SenderType senderType,
    String content,
    LocalDateTime timestamp
){
    public ChatMessageDto(Long messageId, Long id, SenderType senderType, String content, LocalDateTime timestamp){
        this.messageId = messageId;
        this.id = id;
        this.senderType = senderType;
        this.content = content;
        this.timestamp = timestamp;

    }
}

