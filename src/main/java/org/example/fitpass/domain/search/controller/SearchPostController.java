package org.example.fitpass.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.dto.PageResponse;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.search.service.SearchPostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "SEARCH API", description = "검색에 대한 설명입니다.")
public class SearchPostController {

    private final SearchPostService searchPostService;

    @Operation(summary = "게시물 검색",
            description =  "필요 파라미터 = 검색어, 검색어를 통해 내용이나 제목에서 동일어가 있으면 검색")
    @Parameter(name = "keyword", description = "검색어")
    @GetMapping("/search/posts")
    public ResponseEntity<ResponseMessage<PageResponse<PostResponseDto>>> searchPost (
            @RequestParam(name = "keyword") String keyword,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ){
        searchPostService.saveSearchKeywordPost(keyword);

        Page<PostResponseDto> page = searchPostService.searchPost(keyword, pageable);
        PageResponse<PostResponseDto> pageResponse = new PageResponse<>(page);


        ResponseMessage<PageResponse<PostResponseDto>> responseMessage = ResponseMessage.success(SuccessCode.POST_SEARCH_SUCCESS, pageResponse);
        return ResponseEntity.status(SuccessCode.POST_SEARCH_SUCCESS.getHttpStatus()).body(responseMessage);
    }

}
