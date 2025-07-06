package org.example.fitpass.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.comment.dto.request.CommentRequestDto;
import org.example.fitpass.domain.comment.dto.request.CommentUpdateRequestDto;
import org.example.fitpass.domain.comment.dto.response.CommentResponseDto;
import org.example.fitpass.domain.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
@Tag(name="COMMENT API", description = "댓글 관련 API입니다.")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "게시물에 댓글을 작성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 작성 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping
    public ResponseEntity<ResponseMessage<Void>> createComment(
        @Parameter(description = "게시물 ID", required = true)
        @PathVariable Long postId,
        @RequestBody CommentRequestDto requestDto,
        @Parameter(hidden = true)
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.createComment(postId, userDetails.getId(), requestDto.content(), requestDto.parentId());

        return ResponseEntity.status(SuccessCode.COMMENT_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.COMMENT_CREATE_SUCCESS));
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시물의 댓글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
        @ApiResponse(responseCode = "404", description = "게시물이 존재하지 않음")
    })
    @GetMapping
    public ResponseEntity<ResponseMessage<List<CommentResponseDto>>> getComments(
        @Parameter(description = "게시물 ID", required = true)
        @PathVariable Long postId
    ) {
        List<CommentResponseDto> response = commentService.getComments(postId);
        return ResponseEntity.status(SuccessCode.COMMENT_SEARCH_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.COMMENT_SEARCH_SUCCESS, response));
    }

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다. 댓글 작성자만 수정할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
        @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    @PatchMapping("/{commentId}")
    public ResponseEntity<ResponseMessage<Void>> updateComment(
        @Parameter(description = "댓글 ID", required = true)
        @PathVariable Long commentId,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable Long postId,
        @RequestBody CommentUpdateRequestDto dto,
        @Parameter(hidden = true)
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.updateComment(commentId, dto.content(), userDetails.getId(), postId);
        return ResponseEntity.status(SuccessCode.COMMENT_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.COMMENT_UPDATE_SUCCESS));
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 댓글 작성자 또는 게시글 작성자만 삭제할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "댓글이 존재하지 않음")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseMessage<Void>> deleteComment(
        @Parameter(description = "댓글 ID", required = true)
        @PathVariable Long commentId,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable Long postId,
        @Parameter(hidden = true)
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        commentService.deleteComment(commentId, userDetails.getId(), postId);
        return ResponseEntity.status(SuccessCode.COMMENT_DELETE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.COMMENT_DELETE_SUCCESS));
    }
}
