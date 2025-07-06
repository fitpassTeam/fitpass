package org.example.fitpass.domain.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

@Schema(description = "게시물 이미지 포함 응답 DTO")
public record PostImageResponseDto(

    @Schema(description = "게시물 ID", example = "1")
    Long postId,

    @Schema(description = "게시물 이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    List<String> postImage,

    @Schema(description = "게시물 상태 (공개/비공개)", example = "PUBLIC")
    PostStatus status,

    @Schema(description = "게시물 유형 (예: 공지사항, 일반글)", example = "NORMAL")
    PostType postType,

    @Schema(description = "게시물 제목", example = "헬스장 분위기 좋네요!")
    String title,

    @Schema(description = "게시물 내용", example = "오늘 처음 방문했는데 기구 상태도 좋고 조용해서 마음에 들었어요.")
    String content,

    @Schema(description = "작성자 유저 ID", example = "10")
    Long userId,

    @Schema(description = "관련 체육관 ID", example = "3")
    Long gymId,

    @Schema(description = "게시물 생성 시각", example = "2025-07-06T10:15:30")
    LocalDateTime createdAt,

    @Schema(description = "게시물 수정 시각", example = "2025-07-06T11:00:00")
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
        return new PostImageResponseDto(postId, postImage, status, postType, title, content, userId, gymId, createdAt, updatedAt);
    }
}