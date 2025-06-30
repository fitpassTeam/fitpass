package org.example.fitpass.domain.search.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.dto.PageResponse;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.search.service.SearchGymService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search/gyms")
public class SearchGymController {

    private final SearchGymService searchService;
//    private final TestService testService;

    @GetMapping("/v1")
    public ResponseEntity<ResponseMessage<PageResponse<GymResDto>>> searchGym (
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "city", required = false) String city,
        @RequestParam(name = "district", required = false) String district,
        @PageableDefault(page = 0, size = 20) Pageable pageable
    ){
        System.out.printf("👉 [프론트 요청] keyword=%s, city=%s, district=%s, page=%d, size=%d%n",
            keyword, city, district, pageable.getPageNumber(), pageable.getPageSize());
        System.out.println("keyword=" + keyword);
        System.out.println("city=" + city);
        System.out.println("district=" + district);
        System.out.println("pageable=" + pageable);

        searchService.saveSearchKeywordGym(keyword);

        System.out.println("pageable: " + pageable);

        Page<GymResDto> page = searchService.searchGym(keyword, city, district, pageable);
        PageResponse<GymResDto> pageResponse = new PageResponse<>(page);

        ResponseMessage<PageResponse<GymResDto>> responseMessage = ResponseMessage.success(SuccessCode.GYM_SEARCH_SUCCESS, pageResponse);
        return ResponseEntity.status(SuccessCode.GYM_SEARCH_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    @GetMapping("/popular")
    public ResponseEntity<ResponseMessage<List<String>>> searchGymPopular (
    ){
        List<String> response = searchService.searchPopularGym();
        ResponseMessage<List<String>> responseMessage = ResponseMessage.success(SuccessCode.GET_POPULAR_KEYWORD_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.GYM_SEARCH_SUCCESS.getHttpStatus()).body(responseMessage);
    }


//    @GetMapping("/search/gyms/v2")
//    public ResponseEntity<ResponseMessage<PageResponse<GymResponseDto>>> searchGym2 (
//            @RequestParam(name = "keyword") String keyword,
//            @PageableDefault(page = 0, size = 20) Pageable pageable
//    ){
//        testService.saveSearchKeyword(keyword);
//
//        Page<GymResponseDto> responseDto = testService.searchGym2(keyword, pageable);
//        PageResponse<GymResponseDto> pageResponse = new PageResponse<>(page);

//        ResponseMessage<PageResponse<GymResponseDto>> responseMessage = ResponseMessage.success(SuccessCode.GYM_SEARCH_SUCCESS, responseDto);
//        return ResponseEntity.status(SuccessCode.GYM_SEARCH_SUCCESS.getHttpStatus()).body(responseMessage);
//    }

}
