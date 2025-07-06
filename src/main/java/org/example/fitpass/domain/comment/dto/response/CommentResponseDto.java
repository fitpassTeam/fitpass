package org.example.fitpass.domain.comment.dto.response;

import java.util.List;
import org.example.fitpass.domain.comment.entity.Comment;

public record CommentResponseDto(
    Long id,
    String content,
    String name,
    Long writerId,
    Long postOwnerId,
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
