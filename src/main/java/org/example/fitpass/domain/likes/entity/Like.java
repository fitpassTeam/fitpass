package org.example.fitpass.domain.likes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.entity.BaseEntity;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.user.entity.User;

@NoArgsConstructor
@Getter
@Entity(name = "likes")
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LikeType likeType;

    @Column(nullable = false)
    private Long targetId;

    public Like(User user, LikeType likeType, Long targetId){
        this.user = user;
        this.likeType = likeType;
        this.targetId = targetId;
    }

    public static Like of(User user, LikeType likeType, Long targetId){
        return new Like(user, likeType, targetId);
    }

}
