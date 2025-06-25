package org.example.fitpass.domain.post.controller;


import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.dto.PageResponse;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
import org.example.fitpass.domain.post.dto.request.PostUpdateRequestDto;
import org.example.fitpass.domain.post.dto.response.PostImageResponseDto;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;


@RestController
@RequiredArgsConstructor
@RequestMapping("gyms/{gymId}")
public class PostController {

    private final PostService postService;

    //게시물 생성
    @PostMapping("/posts")
    public ResponseEntity<ResponseMessage<PostResponseDto>> creatPost(
        @RequestBody PostCreateRequestDto request,
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId) {
        PostResponseDto postResponseDto = postService.createPost(
                request.status(),
                request.postType(),
                request.postImage(),
                request.title(),
                request.content(),
                user.getId(),
                gymId
        );
        return ResponseEntity.status(SuccessCode.POST_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POST_CREATE_SUCCESS, postResponseDto));
    }

    //General 게시물 전체조회
    @GetMapping("/general-posts")
    public ResponseEntity<ResponseMessage<PageResponse<PostResponseDto>>> findAllGeneralPost(
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId
    ) {
        Page<PostResponseDto> findAllGeneralPost = postService.findAllPostByGeneral(pageable, user.getUser(), gymId, PostType.GENERAL);
        PageResponse<PostResponseDto> pageResponse = new PageResponse<>(findAllGeneralPost);

        return ResponseEntity.status(SuccessCode.GET_ALL_GENERAL_POST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ALL_GENERAL_POST_SUCCESS, pageResponse));
    }

    //Notice 게시물 전체조회
    @GetMapping("/notice-posts")
    public ResponseEntity<ResponseMessage<List<PostResponseDto>>> findAllNoticePost(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("gymId") Long gymId
    ) {
        List<PostResponseDto> findAllNoticePost = postService.findAllPostByNotice(user.getUser(), gymId, PostType.NOTICE);

        return ResponseEntity.status(SuccessCode.GET_ALL_NOTICE_POST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ALL_NOTICE_POST_SUCCESS, findAllNoticePost));
    }


    //게시물 단건 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ResponseMessage<PostImageResponseDto>> findPostById(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @PathVariable("postId") Long postId
    ) {
        PostImageResponseDto findPostById = postService.findPostById(user.getUser(), gymId, postId);

        return ResponseEntity.status(SuccessCode.GET_ONLY_POST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ONLY_POST_SUCCESS, findPostById));
    }

    @PutMapping("/posts/{postId}/photo")
    public ResponseEntity<ResponseMessage<List<String>>> updatePhoto(
            @RequestParam("images")List<MultipartFile> files,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user) {
        List<String> updatedImageUrls = postService.updatePhoto(files, postId, user.getId());
        return ResponseEntity.status(SuccessCode.POST_EDIT_PHOTO_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.POST_EDIT_PHOTO_SUCCESS, updatedImageUrls));
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<ResponseMessage<PostResponseDto>> updatePost(
        @RequestBody PostUpdateRequestDto request,
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @PathVariable("postId") Long postId
    ) {
        PostResponseDto updateDto = postService.updatePost(
                postId,
                request.status(),
                request.postType(),
                request.title(),
                request.content(),
                user.getId(),
                gymId
        );

        return ResponseEntity.status(SuccessCode.POST_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POST_UPDATE_SUCCESS, updateDto));
    }

}
