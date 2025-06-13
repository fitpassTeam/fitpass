package org.example.fitpass.domain.post.controller;


import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
import org.example.fitpass.domain.post.dto.request.PostUpdateRequestDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.service.PostService;
import org.example.fitpass.common.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("gyms/{gymId}")
public class PostController {

    private final PostService postService;

    //게시물 생성
    @PostMapping("/posts")
    public ResponseEntity<ResponseMessage<PostResponseDto>> creatPost(
        @RequestBody PostCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId

    ) {
        PostResponseDto postResponseDto = postService.createPost(requestDto, user.getUser(), gymId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.success(
            SuccessCode.POST_CREATE_SUCCESS, postResponseDto);

        return ResponseEntity.status(SuccessCode.POST_CREATE_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    //General 게시물 전체조회
    @GetMapping("/general-posts")
    public ResponseEntity<ResponseMessage<Page<PostResponseDto>>> findAllGeneralPost(
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId
    ) {
        Page<PostResponseDto> findAllGeneralPost = postService.findAllPostByGeneral(pageable, user.getUser(), gymId, PostType.GENERAL);

        ResponseMessage<Page<PostResponseDto>> responseMessage = ResponseMessage.success(SuccessCode.GET_ALL_GENERAL_POST_SUCCESS, findAllGeneralPost);

        return ResponseEntity.status(SuccessCode.GET_ALL_GENERAL_POST_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    //Notice 게시물 전체조회
    @GetMapping("/notice-posts")
    public ResponseEntity<ResponseMessage<List<PostResponseDto>>> findAllNoticePost(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("gymId") Long gymId
    ) {
        List<PostResponseDto> findAllNoticePost = postService.findAllPostByNotice(user.getUser(), gymId, PostType.NOTICE);

        ResponseMessage<List<PostResponseDto>> responseMessage = ResponseMessage.success(SuccessCode.GET_ALL_NOTICE_POST_SUCCESS, findAllNoticePost);

        return ResponseEntity.status(SuccessCode.GET_ALL_NOTICE_POST_SUCCESS.getHttpStatus()).body(responseMessage);
    }


    //게시물 단건 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ResponseMessage<PostResponseDto>> findPostById(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @PathVariable("postId") Long postId
    ) {
        PostResponseDto findPostById = postService.findPostById(user.getUser(), gymId, postId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.success(
            SuccessCode.GET_ONLY_POST_SUCCESS, findPostById);

        return ResponseEntity.status(SuccessCode.GET_ONLY_POST_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<ResponseMessage<PostResponseDto>> updatePost(
        @RequestBody PostUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @PathVariable("postId") Long postId
    ) {
        PostResponseDto updateDto = postService.updatePost(requestDto, user.getUser(), gymId,
            postId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.success(
            SuccessCode.POST_UPDATE_SUCCESS, updateDto);

        return ResponseEntity.status(SuccessCode.POST_UPDATE_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

}
