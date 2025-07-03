package org.example.fitpass.domain.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

public record PostImageResponseDto(
        Long postId,
        List<String> postImage,
        PostStatus status,
        PostType postType,
        String title,
        String content,
        Long userId,
        Long gymId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static PostImageResponseDto from(Long postId,List<String> postImage, PostStatus status, PostType postType, String title, String content, Long userId, Long gymId, LocalDateTime createdAt, LocalDateTime updatedAt){
        return new PostImageResponseDto(postId, postImage,status, postType, title, content, userId, gymId, createdAt, updatedAt);
    }
}
