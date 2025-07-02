package org.example.fitpass.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.enums.SenderType;

@Schema(description = "채팅 메시지 응답 DTO")
public record ChatMessageResponseDto(
    @Schema(description = "메시지 타입", example = "MESSAGE", allowableValues = {"MESSAGE", "JOIN", "LEAVE"})
    String type,
    
    @Schema(description = "메시지 ID", example = "1")
    Long id,
    
    @Schema(description = "발신자 ID", example = "1")
    Long senderId,
    
    @Schema(description = "발신자 타입", example = "USER")
    SenderType senderType,
    
    @Schema(description = "메시지 내용", example = "안녕하세요! 운동 스케줄 문의드립니다.")
    String content,
    
    @Schema(description = "메시지 생성 시간", example = "2025-07-03T10:30:00")
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
