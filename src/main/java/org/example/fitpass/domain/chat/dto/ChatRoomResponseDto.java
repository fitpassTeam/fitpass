package org.example.fitpass.domain.chat.dto;

import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.enums.SenderType;

public record ChatRoomResponseDto(
    Long chatRoomId,
    Long userId,    // ← 변경
    Long gymId,     // ← 변경
    String content,
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
