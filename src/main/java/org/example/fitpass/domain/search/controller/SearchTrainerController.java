package org.example.fitpass.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.dto.PageResponse;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.search.service.SearchTrainerService;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
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
public class SearchTrainerController {

    private final SearchTrainerService searchTrainerService;

    @Operation(summary = "트레이너 검색",
            description =  "필요 파라미터 = 검색어, 검색어를 통해 이름에서 동일어가 있으면 검색")
    @Parameter(name = "keyword", description = "검색어")
    @GetMapping("/search/trainers")
    public ResponseEntity<ResponseMessage<PageResponse<TrainerResponseDto>>> searchTrainer (
            @RequestParam(name = "keyword") String keyword,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ){
        searchTrainerService.saveSearchKeywordTrainer(keyword);

        Page<TrainerResponseDto> page = searchTrainerService.searchTrainer(keyword, pageable);
        PageResponse<TrainerResponseDto> pageResponse = new PageResponse<>(page);

        ResponseMessage<PageResponse<TrainerResponseDto>> responseMessage = ResponseMessage.success(SuccessCode.TRAINER_SEARCH_SUCCESS, pageResponse);
        return ResponseEntity.status(SuccessCode.TRAINER_SEARCH_SUCCESS.getHttpStatus()).body(responseMessage);
    }

}
