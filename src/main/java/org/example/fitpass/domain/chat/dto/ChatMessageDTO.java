package org.example.fitpass.domain.chat.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.chat.enums.SenderType;

@Getter
@RequiredArgsConstructor
public class ChatMessageDTO {
    private final Long messageId;
    private final Long senderId;
    private final SenderType senderType;
    private final String content;
    private final LocalDateTime timestamp;
}

