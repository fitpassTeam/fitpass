package org.example.fitpass.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.example.fitpass.domain.chat.enums.SenderType;

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
    private Long Id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SenderType senderType; // USER 또는 TRAINER

    @Column(nullable = false)
    private String content;

    public ChatMessage(ChatRoom chatRoom, Long Id, String content, SenderType senderType) {
        this.chatRoom = chatRoom;
        this.Id = Id;
        this.content = content;
        this.senderType = senderType;
    }

    public static ChatMessage of(ChatRoom chatRoom, Long Id, String content, SenderType senderType) {
        return new ChatMessage(chatRoom, Id, content, senderType);
    }
}
