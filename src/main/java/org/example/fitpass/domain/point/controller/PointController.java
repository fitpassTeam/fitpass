package org.example.fitpass.domain.point.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.ResponseMessage;
import org.example.fitpass.domain.point.dto.request.PointCashOutRequestDto;
import org.example.fitpass.domain.point.dto.request.PointChargeRequestDto;
import org.example.fitpass.domain.point.dto.request.PointUseRefundRequestDto;
import org.example.fitpass.domain.point.dto.response.PointCashOutResponseDto;
import org.example.fitpass.domain.point.dto.response.PointResponseDto;
import org.example.fitpass.domain.point.dto.request.PointUseRequestDto;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.service.PointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("users/{userId}/points")
public class PointController {

    private final PointService pointService;

    // 포인트 충전
    @PostMapping("/charge")
    public ResponseEntity<ResponseMessage<Integer>> chargePoint(
        @RequestBody PointChargeRequestDto pointChargeRequestDto,
        @PathVariable Long userId
    ) {
        int newBalance = pointService.chargePoint(userId, pointChargeRequestDto, "포인트 충전");

        ResponseMessage<Integer> responseMessage = ResponseMessage.<Integer>builder()
            .statusCode(HttpStatus.OK.value())
            .message("포인트 충전이 완료되었습니다.")
            .data(newBalance)
            .build();
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // 포인트 사용
    @PostMapping("/use")
    public ResponseEntity<ResponseMessage<Integer>> usePoint(
        @RequestBody PointUseRefundRequestDto pointUseRefundRequestDto,
        @PathVariable Long userId
    ) {
        int newBalance = pointService.usePoint(userId, pointUseRefundRequestDto);

        ResponseMessage<Integer> responseMessage = ResponseMessage.<Integer>builder()
            .statusCode(HttpStatus.OK.value())
            .message("포인트가 사용되었습니다.")
            .data(newBalance)
            .build();
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // 포인트 100% 환불 (PT 취소)
    @PostMapping("/refund")
    public ResponseEntity<ResponseMessage<Integer>> refundPoint(
        @RequestBody PointUseRefundRequestDto pointUseRefundRequestDto,
        @PathVariable Long userId
    ) {
        int newBalance = pointService.refundPoint(userId, pointUseRefundRequestDto);

        ResponseMessage<Integer> responseMessage = ResponseMessage.<Integer>builder()
            .statusCode(HttpStatus.OK.value())
            .message("포인트가 환불되었습니다.")
            .data(newBalance)
            .build();
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // 포인트 현금화 (90% 현금 환불)
    @PostMapping("/cashout")
    public ResponseEntity<ResponseMessage<PointCashOutResponseDto>> cashOutPoint(
        @RequestBody PointCashOutRequestDto pointCashOutRequestDto,
        @PathVariable Long userId
    ) {
        PointCashOutResponseDto pointCashOutResponseDto = pointService.cashOutPoint(userId, pointCashOutRequestDto);

        ResponseMessage<PointCashOutResponseDto> responseMessage = ResponseMessage.<PointCashOutResponseDto>builder()
            .statusCode(HttpStatus.OK.value())
            .message("포인트 현금화가 완료되었습니다.")
            .data(pointCashOutResponseDto)
            .build();
        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // 포인트 잔액 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<Integer>> getPointBalance (
        @PathVariable Long userId) {
        int balance = pointService.getPointBalance(userId);
        ResponseMessage<Integer> responseMessage = ResponseMessage.<Integer>builder()
            .statusCode(HttpStatus.OK.value())
            .message("포인트 잔액이 조회되었습니다.")
            .data(balance)
            .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    // 포인트 이력 조회
    @GetMapping("/history")
    public ResponseEntity<ResponseMessage<List<PointResponseDto>>> getPointHistory (@PathVariable Long userId) {
        List<Point> history = pointService.getPointHistory(userId);
        List<PointResponseDto> pointResponseDtos = history.stream()
            .map(PointResponseDto::from)
            .collect(Collectors.toList());

        ResponseMessage<List<PointResponseDto>> responseMessage = ResponseMessage.<List<PointResponseDto>>builder()
            .statusCode(HttpStatus.OK.value())
            .message("포인트 이력이 조회되었습니다.")
            .data(pointResponseDtos)
            .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }


}
