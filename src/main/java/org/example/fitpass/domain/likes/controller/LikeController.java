package org.example.fitpass.domain.likes.controller;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.dto.PageResponse;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.likes.service.LikeService;
import org.example.fitpass.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/gyms/{gymId}/like")
    public ResponseEntity<ResponseMessage<Void>> postGymLike(
        @PathVariable Long gymId,
        @AuthenticationPrincipal CustomUserDetails user
        ){
        likeService.postGymLike(user.getId(), gymId);
        return ResponseEntity.status(SuccessCode.LIKE_TOGGLE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.LIKE_TOGGLE_SUCCESS));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ResponseMessage<Void>> postLike(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomUserDetails user
    ){
        likeService.postLike(user.getId(), postId);
        return ResponseEntity.status(SuccessCode.LIKE_TOGGLE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.LIKE_TOGGLE_SUCCESS));
    }
}
