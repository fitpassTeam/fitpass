package org.example.fitpass.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.example.fitpass.domain.comment.entity.Comment;

@Schema(description = "댓글 응답 DTO")
public record CommentResponseDto(

    @Schema(description = "댓글 ID", example = "101")
    Long id,

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!")
    String content,

    @Schema(description = "작성자 이름", example = "홍길동")
    String name,

    @Schema(description = "작성자 ID", example = "12")
    Long writerId,

    @Schema(description = "게시물 주인 ID", example = "10")
    Long postOwnerId,

    @Schema(description = "대댓글 목록")
    List<CommentResponseDto> children

) {
    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
            comment.getId(),
            comment.getContent(),
            comment.getUser().getName(),
            comment.getUser().getId(),
            comment.getPost().getUser().getId(),
            comment.getChildren().stream()
                .map(CommentResponseDto::from)
                .toList()
        );
    }
}