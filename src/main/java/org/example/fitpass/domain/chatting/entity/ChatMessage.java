package org.example.fitpass.domain.chatting.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;

@Getter
@NoArgsConstructor
@Table(name = "chatMessage")
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;
    private String msg;
    private String imageUrl;
    private int roomNumber;

    public ChatMessage(String userName, String msg, String imageUrl, int roomNumber){
        this.userName = userName;
        this.msg = msg;
        this.imageUrl = imageUrl;
        this.roomNumber = roomNumber;
    }
}
