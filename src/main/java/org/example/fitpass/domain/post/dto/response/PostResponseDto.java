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
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PostResponseDto {

    private Long postId;
    private PostStatus status;
    private PostType postType;
    private String title;
    private String content;
    private String postImage;
    private Long userId;
    private Long gymId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
