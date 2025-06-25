package org.example.fitpass.domain.search.controller;

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
public class SearchPostController {

    private final SearchPostService searchPostService;

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
