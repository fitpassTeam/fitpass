package org.example.fitpass.domain.chat.dto;

import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.enums.SenderType;

public record ChatRoomResponseDto(
    Long chatRoomId,
    Long senderId,
    Long receiverId,
    String content,
    SenderType senderType
) {
    public ChatRoomResponseDto(Long chatRoomId, Long senderId, Long receiverId, String content, SenderType senderType){
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.senderType = senderType;
    }

    public static ChatRoomResponseDto from(ChatRoom chatRoom, ChatMessage lastMessage){
        return new ChatRoomResponseDto(
            chatRoom.getId(),
            chatRoom.getUser().getId(),
            chatRoom.getTrainer().getId(),
            lastMessage != null ? lastMessage.getContent() : null,
            lastMessage != null ? lastMessage.getSenderType() : null
        );
    }

}
