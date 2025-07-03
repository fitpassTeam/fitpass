package org.example.fitpass.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.enums.SenderType;

@Schema(description = "채팅방 정보 응답 DTO")
public record ChatRoomResponseDto(
    @Schema(description = "채팅방 ID", example = "1")
    Long chatRoomId,
    
    @Schema(description = "회원 ID", example = "1")
    Long userId,    // ← 변경
    
    @Schema(description = "체육관 ID", example = "2")
    Long gymId,     // ← 변경
    
    @Schema(description = "마지막 메시지 내용", example = "내일 오후 3시에 PT 가능한가요?")
    String content,
    
    @Schema(description = "마지막 메시지 발신자 타입", example = "USER")
    SenderType senderType
) {
    public ChatRoomResponseDto(Long chatRoomId, Long userId, Long gymId, String content, SenderType senderType){
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.gymId = gymId;
        this.content = content;
        this.senderType = senderType;
    }

    public static ChatRoomResponseDto from(ChatRoom chatRoom, ChatMessage lastMessage){
        return new ChatRoomResponseDto(
            chatRoom.getId(),
            chatRoom.getUser().getId(), // userId
            chatRoom.getGym().getId(),  // gymId
            lastMessage != null ? lastMessage.getContent() : null,
            lastMessage != null ? lastMessage.getSenderType() : null
        );
    }
}
