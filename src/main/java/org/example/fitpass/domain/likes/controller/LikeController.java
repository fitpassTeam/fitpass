package org.example.fitpass.domain.likes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.likes.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "LIKE API", description = "게시물 및 체육관 좋아요 처리 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class LikeController {

    private final LikeService likeService;

    @Operation(
        summary = "체육관 좋아요 토글",
        description = "사용자가 특정 체육관에 좋아요를 누르거나 취소합니다. 이미 눌렀다면 취소됩니다."
    )
    @PostMapping("/gyms/{gymId}/like")
    public ResponseEntity<ResponseMessage<Void>> postGymLike(
        @Parameter(description = "좋아요를 누를 체육관 ID", required = true)
        @PathVariable Long gymId,
        @Parameter(hidden = true) // Swagger UI에 노출되지 않도록
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        likeService.postGymLike(user.getId(), gymId);
        return ResponseEntity.status(SuccessCode.LIKE_TOGGLE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.LIKE_TOGGLE_SUCCESS));
    }

    @Operation(
        summary = "게시물 좋아요 토글",
        description = "사용자가 특정 게시물에 좋아요를 누르거나 취소합니다. 이미 눌렀다면 취소됩니다."
    )
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ResponseMessage<Void>> postLike(
        @Parameter(description = "좋아요를 누를 게시물 ID", required = true)
        @PathVariable Long postId,
        @Parameter(hidden = true)
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        likeService.postLike(user.getId(), postId);
        return ResponseEntity.status(SuccessCode.LIKE_TOGGLE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.LIKE_TOGGLE_SUCCESS));
    }
}