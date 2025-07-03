package org.example.fitpass.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.fitpass.domain.chat.enums.SenderType;

@Schema(description = "채팅 메시지 전송 요청 DTO")
public record ChatMessageRequestDto(
    @Schema(description = "메시지 발신자 ID", example = "1", required = true)
    Long senderId,
    
    @Schema(description = "메시지 수신자 ID", example = "2", required = true)
    Long receiverId,
    
    @Schema(description = "메시지 내용", example = "안녕하세요! 운동 스케줄 문의드립니다.", required = true)
    String message,
    
    @Schema(description = "발신자 타입", example = "USER", required = true)
    SenderType senderType
) {
} 
