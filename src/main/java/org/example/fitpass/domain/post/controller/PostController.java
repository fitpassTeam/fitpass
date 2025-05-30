package org.example.fitpass.domain.post.controller;


import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
import org.example.fitpass.domain.post.dto.request.PostUpdateRequestDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/gyms/{gymId}")
public class PostController {

    private final PostService postService;

    //게시물 생성
    @PostMapping("/posts")
    public ResponseEntity<ResponseMessage<PostResponseDto>> creatPost(@RequestBody PostCreateRequestDto requestDto, @AuthenticationPrincipal CustomUserPrincipal user, @PathVariable Long gymId

    ) {
        PostResponseDto postResponseDto = postService.createPost(requestDto, user, gymId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.success(SuccessCode.POST_CREATE_SUCCESS, postResponseDto);

        return ResponseEntity.status(SuccessCode.POST_CREATE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    //게시물 전체조회
    @GetMapping("/posts")
    public ResponseEntity<ResponseMessage<Page<PostResponseDto>>> findAllPost(@PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @AuthenticationPrincipal CustomUserPrincipal user, @PathVariable Long gymId) {
        Page<PostResponseDto> findAllPost = postService.findAllPost(pageable, user.getId(), gymId);

        ResponseMessage<Page<PostResponseDto>> responseMessage = ResponseMessage.success(SuccessCode.GET_ALL_POST_SUCCESS, findAllPost);

        return ResponseEntity.status(SuccessCode.GET_ALL_POST_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    //게시물 단건 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ResponseMessage<PostResponseDto>> findPostById(@AuthenticationPrincipal CustomUserPrincipal user, @PathVariable Long gymId, @PathVariable Long postId) {
        PostResponseDto findPostById = postService.findPostById(user.getId(), gymId, postId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.success(SuccessCode.GET_ONLY_POST_SUCCESS, findPostById);

        return ResponseEntity.status(SuccessCode.GET_ONLY_POST_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    @PatchMapping("posts/{postId}")
    public ResponseEntity<ResponseMessage<PostResponseDto>> updatePost(@RequestBody PostUpdateRequestDto requestDto, @AuthenticationPrincipal CustomUserPrincipal user, @PathVariable Long gymId, @PathVariable Long postId) {
        PostResponseDto updateDto = postService.updatePost(requestDto, user.getId(), gymId, postId);

        ResponseMessage<PostResponseDto> responseMessage = ResponseMessage.success(SuccessCode.POST_UPDATE_SUCCESS, updateDto);

        return ResponseEntity.status(SuccessCode.POST_UPDATE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

}
