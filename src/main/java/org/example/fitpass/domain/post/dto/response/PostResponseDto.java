package org.example.fitpass.domain.post.dto.response;

import lombok.Getter;
import org.example.fitpass.common.entity.Image;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public record PostResponseDto(
        Long postId,
        PostStatus status,
        PostType postType,
        String title,
        String content,
        Long userId,
        Long gymId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostResponseDto of(Long postId, PostStatus status, PostType postType, String title, String content, Long userId, Long gymId, LocalDateTime createdAt, LocalDateTime updatedAt){
        return new PostResponseDto(postId, status, postType, title, content, userId, gymId, createdAt, updatedAt);
    }

    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getPostStatus(),
                post.getPostType(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getId(),
                post.getGym().getId(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

}
