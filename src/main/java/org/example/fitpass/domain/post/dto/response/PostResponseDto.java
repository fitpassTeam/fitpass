package org.example.fitpass.domain.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

import java.time.LocalDateTime;

public record PostResponseDto(
        @Schema(description = "게시물 ID", example = "1")
        Long postId,

        @Schema(description = "게시물 상태 (ACTIVE, DELETED)", example = "ACTIVE")
        PostStatus status,

        @Schema(description = "게시물 타입 (NOTICE, GENERAL)", example = "GENERAL")
        PostType postType,

        @Schema(description = "게시물 제목", example = "제목 테스트")
        String title,

        @Schema(description = "게시물 내용", example = "내용 테스트")
        String content,

        @Schema(description = "유저 ID", example = "1")
        Long userId,

        @Schema(description = "체육관 ID", example = "1")
        Long gymId,

        @Schema(description = "생성 시간")
        LocalDateTime createdAt,

        @Schema(description = "수정 시간")
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
