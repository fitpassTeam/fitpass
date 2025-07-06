package org.example.fitpass.domain.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지가 포함된 게시물 응답 DTO")
public record PostImageResponseDto(

    @Schema(description = "게시물 ID", example = "1")
    Long postId,

    @Schema(description = "게시물 이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    List<String> postImage,

    @Schema(description = "게시물 상태", example = "ACTIVE")
    PostStatus status,

    @Schema(description = "게시물 타입", example = "GENERAL")
    PostType postType,

    @Schema(description = "게시물 제목", example = "오늘의 운동 공유합니다")
    String title,

    @Schema(description = "게시물 내용", example = "오늘은 하체를 중점적으로 했습니다.")
    String content,

    @Schema(description = "작성자 ID", example = "100")
    Long userId,

    @Schema(description = "헬스장 ID", example = "10")
    Long gymId,

    @Schema(description = "생성일시", example = "2024-07-05T12:34:56")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2024-07-05T14:20:00")
    LocalDateTime updatedAt

) {
    public static PostImageResponseDto from(
        Long postId,
        List<String> postImage,
        PostStatus status,
        PostType postType,
        String title,
        String content,
        Long userId,
        Long gymId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new PostImageResponseDto(
            postId, postImage, status, postType, title, content, userId, gymId, createdAt, updatedAt
        );
    }
}