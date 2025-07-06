package org.example.fitpass.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 수정 요청 DTO")
public record CommentUpdateRequestDto(
    @Schema(description = "수정할 댓글", example = "잘 보고 갑니다.")
    String content
) {}
