package org.example.fitpass.domain.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "post",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> postImages = new ArrayList<>();

    public Post(PostStatus postStatus, PostType postType, List<Image> postImage, String title, String content, User user, Gym gym) {
        this.postStatus = postStatus;
        this.postType = postType;
        this.postImages = postImage;
        this.title = title;
        this.content = content;
        this.user = user;
        this.gym = gym;

        for (Image image : postImage) {
            image.assignToPost(this);
            this.postImages.add(image);
        }
    }

    public Post(PostStatus postStatus, PostType postType, String title, String content, User user, Gym gym) {
        this.postStatus = postStatus;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.user = user;
        this.gym = gym;


    }


    public void update(PostStatus postStatus, PostType postType, String title, String content) {
        this.postStatus = postStatus;
        this.postType = postType;
        this.title = title;
        this.content = content;

    }

    public static Post of(PostStatus postStatus, PostType postType, List<String> postImage, String title, String content, User user, Gym gym) {
        Post post = new Post(postStatus, postType, title, content, user, gym);

        if(postImage != null && !postImage.isEmpty()){
            for(String photo : postImage){
                Image image = new Image(photo, post);
                post.getPostImages().add(image);
            }
        }
        return post;
    }

    public void isOwner(Long userId) {
        if (!this.user.getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_POST_OWNER);
        }
    }

    public void updatePhoto(List<String> imageUrls, Post post) {
        this.postImages.clear();
        List<Image> convertedImages = imageUrls.stream()
                .map( url -> Image.from(url,post))
                .toList();
        this.postImages.addAll(convertedImages);
    }

}
