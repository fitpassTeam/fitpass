package org.example.fitpass.domain.post.entity;

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
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.user.entity.User;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PostStatus postStatus;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    private String postImage;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    public Post(PostStatus postStatus, PostType postType, String postImage, String title, String content, User user, Gym gym) {
        this.postStatus = postStatus;
        this.postType = postType;
        this.postImage = postImage;
        this.title = title;
        this.content = content;
        this.user = user;
        this.gym = gym;
    }

    public void  update(String title, String content, String postImage) {
        this.title = title;
        this.content = content;
        this.postImage = postImage;
    }
}
