package org.example.fitpass.domain.post.controller;


import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.ResponseMessage;
import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
import org.example.fitpass.domain.post.dto.request.PostUpdateRequestDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gyms/{gymsId}")
public class PostController {

    private final PostService postService;

    //게시물 생성
    @PostMapping("/posts")
    public ResponseEntity<ResponseMessage<PostResponseDto>> creatPost(
            @RequestBody PostCreateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable Long gymId

    ){
        PostResponseDto postResponseDto = postService.createPost(requestDto, user, gymId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.<PostResponseDto>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("게시물이 생성 되었습니다.")
                .data(postResponseDto)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
    }

    //게시물 전체조회
    @GetMapping("/posts")
    public ResponseEntity<ResponseMessage<List<PostResponseDto>>> findAllPost(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable Long gymsId
    ){
        List<PostResponseDto> findAllPost = postService.findAllPost(user.getId(), gymsId);

        ResponseMessage<List<PostResponseDto>> responseMessage = ResponseMessage.<List<PostResponseDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("전체 조회에 성공 하였습니다.")
                .data(findAllPost)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    //게시물 단건 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ResponseMessage<PostResponseDto>> findPostById(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable Long gymsId,
            @PathVariable Long postId
    ){
        PostResponseDto findPostById = postService.findPostById(user.getId(), gymsId, postId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.<PostResponseDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("단건 조회에 성공 하였습니다.")
                .data(findPostById)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    @PatchMapping("posts/{postId}")
    public ResponseEntity<ResponseMessage<PostResponseDto>> updatePost(
            @RequestBody PostUpdateRequestDto reqeustDto,
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable Long gymsId,
            @PathVariable Long postId
    ){
        PostResponseDto updateDto = postService.updatePost(reqeustDto, user.getId(), gymsId, postId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.<PostResponseDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("게시물 수정에 성공 하였습니다.")
                .data(updateDto)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

}
