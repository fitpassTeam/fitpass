package org.example.fitpass.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

import java.time.LocalDateTime;

@Getter
public class PostResponseDto {

    private final Long postId;

    private final PostStatus status;

    private final PostType postType;

    private final String title;

    private final String content;

    private final String postImage;

    private final Long userId;

    private final Long gymId;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public PostResponseDto(Long postId, PostStatus status, PostType postType, String title, String content, String postImage, Long userId, Long gymId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.postId = postId;
        this.status = status;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.postImage = postImage;
        this.userId = userId;
        this.gymId = gymId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getPostStatus(),
                post.getPostType(),
                post.getTitle(),
                post.getContent(),
                post.getPostImage(),
                post.getUser().getId(),
                post.getGym().getId(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

}
