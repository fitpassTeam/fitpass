package org.example.fitpass.domain.search.controller;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.search.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/gyms/search")
    public ResponseEntity<ResponseMessage<Page<GymResponseDto>>> searchGym (
            @RequestParam(name = "keyword") String keyword,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ){
        searchService.saveSearchKeyword(keyword);

        Page<GymResponseDto> responseDto = searchService.searchGym(keyword, pageable);

        ResponseMessage<Page<GymResponseDto>> responseMessage = ResponseMessage.success(SuccessCode.GYM_SEARCH_SUCCESS, responseDto);

        return ResponseEntity.status(SuccessCode.GYM_SEARCH_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    @GetMapping("/posts/search")
    public ResponseEntity<ResponseMessage<Page<PostResponseDto>>> searchPost (
            @RequestParam(name = "keyword") String keyword,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ){
        searchService.saveSearchKeyword(keyword);

        Page<PostResponseDto> responseDto = searchService.searchPost(keyword, pageable);

        ResponseMessage<Page<PostResponseDto>> responseMessage = ResponseMessage.success(SuccessCode.POST_SEARCH_SUCCESS, responseDto);

        return ResponseEntity.status(SuccessCode.POST_SEARCH_SUCCESS.getHttpStatus()).body(responseMessage);
    }

}
