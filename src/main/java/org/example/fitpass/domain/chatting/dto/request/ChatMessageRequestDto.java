package org.example.fitpass.domain.chatting.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.chatting.entity.ChatMessage;

@Getter
@NoArgsConstructor
public class ChatMessageRequestDto {

    private String userName;
    private String msg;
    private String imageUrl;
    private int roomNumber;

    public ChatMessageRequestDto(String userName, String msg, String imageUrl, int roomNumber){
        this.userName = userName;
        this.msg = msg;
        this.imageUrl = imageUrl;
        this.roomNumber = roomNumber;
    }

    public ChatMessage toEntity() {
        return new ChatMessage(userName,msg,imageUrl,roomNumber);
    }

}
