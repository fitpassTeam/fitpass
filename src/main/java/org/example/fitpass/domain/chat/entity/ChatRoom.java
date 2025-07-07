package org.example.fitpass.domain.chat.entity;

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
import org.example.fitpass.common.entity.BaseEntity;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chatRooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    public ChatRoom(User user, Gym gym) {
        this.user = user;
        this.gym = gym;
    }

    public static ChatRoom of(User user, Gym gym) {
        return new ChatRoom(user, gym);
    }
}
