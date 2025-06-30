package org.example.fitpass.domain.chat.dto;

import org.example.fitpass.domain.chat.enums.SenderType;

public record ChatMessageRequestDto(
    Long senderId,
    Long receiverId,
    String message,
    SenderType senderType
) {
} 