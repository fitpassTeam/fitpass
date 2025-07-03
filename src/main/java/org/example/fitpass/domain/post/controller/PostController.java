package org.example.fitpass.domain.post.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.dto.PageResponse;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
import org.example.fitpass.domain.post.dto.request.PostUpdateRequestDto;
import org.example.fitpass.domain.post.dto.response.PostImageResponseDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@Tag(name = "POST API", description = "게시물 관리에 대한 설명입니다.")
@RequiredArgsConstructor
@RequestMapping("gyms/{gymId}")
public class PostController {

    private final PostService postService;

    //게시물 생성
    @Operation(summary = "게시물 생성",
        description = "회원 가입이 된 모든 유저들은 게시물을 작성 할 수 있습니다." )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 생성 완료"),
            @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없습니다.")
    })
    @PostMapping("/posts")
    @Parameter(name = "gymId", description = "체육관 ID", required = true)
    public ResponseEntity<ResponseMessage<PostResponseDto>> creatPost(
        @RequestBody PostCreateRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("gymId") Long gymId) {
        PostResponseDto postResponseDto = postService.createPost(
                request.status(),
                request.postType(),
                request.postImage(),
                request.title(),
                request.content(),
            userDetails.getId(),
                gymId
        );
        return ResponseEntity.status(SuccessCode.POST_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POST_CREATE_SUCCESS, postResponseDto));
    }

    //General 게시물 전체조회
    @Operation(summary = "GENERAL 게시물 조회",
        description = "회원 가입을 한 유저들이 GENERAL 게시물을 조회 할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "GENERAL 게시물 조회 성공")
    })
    @GetMapping("/general-posts")
    @Parameter(name = "gymId", description = "체육관 ID", required = true)
    public ResponseEntity<ResponseMessage<PageResponse<PostResponseDto>>> findAllGeneralPost(
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("gymId") Long gymId
    ) {
        Page<PostResponseDto> findAllGeneralPost = postService.findAllPostByGeneral(pageable, userDetails.getUser(), gymId, PostType.GENERAL);
        PageResponse<PostResponseDto> pageResponse = new PageResponse<>(findAllGeneralPost);

        return ResponseEntity.status(SuccessCode.GET_ALL_GENERAL_POST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ALL_GENERAL_POST_SUCCESS, pageResponse));
    }

    //Notice 게시물 전체조회
    @Operation(summary = "NOTICE 게시물 조회",
            description = "회원 가입을 한 유저들이 NOTICE 게시물을 조회 할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "NOTICE 게시물 조회 성공")
    })
    @GetMapping("/notice-posts")
    @Parameter(name = "gymId", description = "체육관 ID", required = true)
    public ResponseEntity<ResponseMessage<List<PostResponseDto>>> findAllNoticePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("gymId") Long gymId
    ) {
        List<PostResponseDto> findAllNoticePost = postService.findAllPostByNotice(userDetails.getUser(), gymId, PostType.NOTICE);

        return ResponseEntity.status(SuccessCode.GET_ALL_NOTICE_POST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ALL_NOTICE_POST_SUCCESS, findAllNoticePost));
    }


    //게시물 단건 조회
    @Operation(summary = "게시물 단건 조회",
            description = "회원 가입을 한 유저들이 단건의 게시물을 조회 할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 조회 성공"),
            @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없습니다.")
    })
    @GetMapping("/posts/{postId}")
    @Parameter(name = "gymId", description = "체육관 ID", required = true)
    @Parameter(name = "postId", description = "게시물 ID", required = true)
    public ResponseEntity<ResponseMessage<PostImageResponseDto>> findPostById(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("gymId") Long gymId,
        @PathVariable("postId") Long postId
    ) {
        PostImageResponseDto findPostById = postService.findPostById(userDetails.getUser(), gymId, postId);

        return ResponseEntity.status(SuccessCode.GET_ONLY_POST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ONLY_POST_SUCCESS, findPostById));
    }
    @Operation(summary = "게시물 이미지 수정",
            description = "게시물을 작성한 한 유저가 게시물의 사진을 수정 할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 조회 성공"),
            @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없습니다.")
    })
    @PatchMapping("/posts/{postId}/photo")
    @Parameter(name = "gymId", description = "체육관 ID", required = true)
    @Parameter(name = "postId", description = "게시물 ID", required = true)
    public ResponseEntity<ResponseMessage<List<String>>> updatePhoto(
            @RequestParam("images")List<MultipartFile> files,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<String> updatedImageUrls = postService.updatePhoto(files, postId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.POST_EDIT_PHOTO_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.POST_EDIT_PHOTO_SUCCESS, updatedImageUrls));
    }

    @Operation(summary = "게시물 수정",
            description = "게시물을 작성한 한 유저가 게시물의 수정 할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시물 조회 성공"),
            @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없습니다.")
    })
    @PatchMapping("/posts/{postId}")
    @Parameter(name = "gymId", description = "체육관 ID", required = true)
    @Parameter(name = "postId", description = "게시물 ID", required = true)
    public ResponseEntity<ResponseMessage<PostResponseDto>> updatePost(
        @RequestBody PostUpdateRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("gymId") Long gymId,
        @PathVariable("postId") Long postId
    ) {
        PostResponseDto updateDto = postService.updatePost(
                postId,
                request.status(),
                request.postType(),
                request.title(),
                request.content(),
            userDetails.getId(),
                gymId
        );

        return ResponseEntity.status(SuccessCode.POST_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POST_UPDATE_SUCCESS, updateDto));
    }

}
