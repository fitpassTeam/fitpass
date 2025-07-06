package org.example.fitpass.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 요청 DTO")
public record CommentRequestDto(
    @Schema(description = "댓글 부모 아이디", example = "1")
    Long parentId,
    @Schema(description = "댓글 내용", example = "잘 보고 갑니다.")
    String content
) {}
