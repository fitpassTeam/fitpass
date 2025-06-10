package org.example.fitpass.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "chatRooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long trainerId;

    public ChatRoom(Long userId, Long trainerId){
        this.userId = userId;
        this.trainerId = trainerId;
    }

    public static ChatRoom of(Long userId, Long trainerId){
       return new ChatRoom(userId, trainerId);
    }
}
