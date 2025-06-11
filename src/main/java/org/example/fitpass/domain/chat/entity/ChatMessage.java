package org.example.fitpass.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chatMessage")
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoomId")
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String content;

    public ChatMessage(ChatRoom chatRoom, Long senderId, String content) {
        this.chatRoom = chatRoom;
        this.senderId = senderId;
        this.content = content;
    }

    public static ChatMessage of(ChatRoom chatRoom, Long senderId, String content) {
        return new ChatMessage(chatRoom, senderId, content);
    }
}
