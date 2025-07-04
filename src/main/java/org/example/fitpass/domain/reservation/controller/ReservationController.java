package org.example.fitpass.domain.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.reservation.dto.request.ReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.request.UpdateReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.response.AllGymReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.GetReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.ReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.TrainerReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.UpdateReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.UserReservationResponseDto;
import org.example.fitpass.domain.reservation.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "예약 관리", description = "트레이너 예약 생성, 수정, 취소 및 조회")
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 가능 시간 조회
    @Operation(
        summary = "예약 가능 시간 조회",
        description = "특정 날짜의 트레이너 예약 가능한 시간대를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 가능 시간 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
        @ApiResponse(responseCode = "404", description = "헬스장 또는 트레이너를 찾을 수 없음")
    })
    @GetMapping("/gyms/{gymId}/trainers/{trainerId}/available-times")
    public ResponseEntity<ResponseMessage<List<LocalTime>>> getAvailableTimes(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate date) {

        List<LocalTime> availableTimes = reservationService.getAvailableTimes(userDetails.getId(), gymId, trainerId, date);

        return ResponseEntity.status(SuccessCode.AVAILABLE_TIMES_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.AVAILABLE_TIMES_GET_SUCCESS, availableTimes));
    }

    // 예약 생성
    @Operation(
        summary = "예약 생성",
        description = "트레이너와의 PT 예약을 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "예약 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "이미 예약된 시간"),
        @ApiResponse(responseCode = "402", description = "포인트 부족")
    })
    @PostMapping("/gyms/{gymId}/trainers/{trainerId}/reservations")
    public ResponseEntity<ResponseMessage<ReservationResponseDto>> createReservation (
        @Valid @RequestBody ReservationRequestDto reservationRequestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long gymId,
        @PathVariable Long trainerId) {
        ReservationResponseDto reservationResponseDto =
            reservationService.createReservation(
                reservationRequestDto.reservationDate(),
                reservationRequestDto.reservationTime(),
                userDetails.getId(),
                gymId,
                trainerId);

        return ResponseEntity.status(SuccessCode.RESERVATION_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_CREATE_SUCCESS, reservationResponseDto));
    }

    // 예약 수정
    @Operation(
        summary = "예약 수정",
        description = "기존 예약의 날짜, 시간, 상태를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @PatchMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<UpdateReservationResponseDto>> updateReservation (
        @Valid @RequestBody UpdateReservationRequestDto updateReservationRequestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ){
        UpdateReservationResponseDto updateReservationResponseDto =
            reservationService.updateReservation(
                updateReservationRequestDto.reservationDate(),
                updateReservationRequestDto.reservationTime(),
                updateReservationRequestDto.reservationStatus(),
                userDetails.getId(),
                gymId,
                trainerId,
                reservationId);

        return ResponseEntity.status(SuccessCode.RESERVATION_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_UPDATE_SUCCESS, updateReservationResponseDto));
    }

    // 예약 취소
    @Operation(
        summary = "예약 취소",
        description = "예약을 취소하고 포인트를 환불합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 취소 및 환불 성공"),
        @ApiResponse(responseCode = "400", description = "취소 불가능한 예약"),
        @ApiResponse(responseCode = "403", description = "취소 권한 없음"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @DeleteMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<Void>> cancelReservation (
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(userDetails.getId(), gymId, trainerId, reservationId);

        return ResponseEntity.status(SuccessCode.RESERVATION_CANCEL_WITH_REFUND_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_CANCEL_WITH_REFUND_SUCCESS));
    }

    // 트레이너별 예약 목록 조회
    @Operation(
        summary = "트레이너별 예약 목록 조회",
        description = "특정 트레이너의 모든 예약 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "트레이너 예약 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "조회 권한 없음"),
        @ApiResponse(responseCode = "404", description = "트레이너를 찾을 수 없음")
    })
    @GetMapping("/gyms/{gymId}/trainers/{trainerId}/reservations")
    public ResponseEntity<ResponseMessage<List<TrainerReservationResponseDto>>> getTrainerReservation (
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long gymId,
        @PathVariable Long trainerId
    ) {
        List<TrainerReservationResponseDto> trainerReservationResponseDto =
            reservationService.getTrainerReservation(userDetails.getId(), gymId, trainerId);

        return ResponseEntity.status(SuccessCode.TRAINER_RESERVATION_LIST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.TRAINER_RESERVATION_LIST_SUCCESS, trainerReservationResponseDto));
    }

    // 체육관 전체 트레이너의 예약 통합 조회
    @Operation(
        summary = "헬스장 전체 예약 조회",
        description = "헬스장 내 모든 트레이너의 예약을 통합하여 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "헬스장 전체 예약 조회 성공"),
        @ApiResponse(responseCode = "403", description = "헬스장 오너 권한 없음"),
        @ApiResponse(responseCode = "404", description = "헬스장을 찾을 수 없음")
    })
    @GetMapping("/gyms/{gymId}/reservations")
    public ResponseEntity<ResponseMessage<List<AllGymReservationResponseDto>>> getGymAllReservations (
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long gymId
    ) {
        List<AllGymReservationResponseDto> reservations =
            reservationService.getGymAllReservations(userDetails.getId(), gymId);

        return ResponseEntity.status(SuccessCode.TRAINER_RESERVATION_LIST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.TRAINER_RESERVATION_LIST_SUCCESS, reservations));
    }

    // 유저별 예약 목록
    @Operation(
        summary = "내 예약 목록 조회",
        description = "현재 사용자의 모든 예약 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 예약 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/users/reservations")
    public ResponseEntity<ResponseMessage<List<UserReservationResponseDto>>> getUserReservations(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        List<UserReservationResponseDto> userReservations =
            reservationService.getUserReservations(userDetails.getId());
        
        return ResponseEntity.status(SuccessCode.USER_RESERVATION_LIST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_RESERVATION_LIST_SUCCESS, userReservations));
    }

    // 예약 단건 조회
    @Operation(
        summary = "예약 상세 조회",
        description = "특정 예약의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 상세 조회 성공"),
        @ApiResponse(responseCode = "403", description = "조회 권한 없음"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<GetReservationResponseDto>> getReservation(
        @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long reservationId) {

        GetReservationResponseDto reservation =
            reservationService.getReservation(userDetails.getId(), reservationId);
        
        return ResponseEntity.status(SuccessCode.RESERVATION_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_GET_SUCCESS, reservation));
    }

    // 트레이너 예약 승인
    @Operation(
        summary = "예약 승인",
        description = "트레이너가 예약을 승인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 승인 성공"),
        @ApiResponse(responseCode = "403", description = "트레이너 권한 없음"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @PatchMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}/confirm")
    public ResponseEntity<ResponseMessage<Void>> confirmReservation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ) {
        reservationService.confirmReservation(userDetails.getId(), gymId, trainerId, reservationId);

        return ResponseEntity.status(SuccessCode.RESERVATION_CONFIRM_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_CONFIRM_SUCCESS));
    }

    // 트레이너 예약 거부
    @Operation(
        summary = "예약 거부",
        description = "트레이너가 예약을 거부합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예약 거부 성공"),
        @ApiResponse(responseCode = "403", description = "트레이너 권한 없음"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @PatchMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}/reject")
    public ResponseEntity<ResponseMessage<Void>> rejectReservation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ) {
        reservationService.rejectReservation(userDetails.getId(), gymId, trainerId, reservationId);

        return ResponseEntity.status(SuccessCode.RESERVATION_REJECT_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_REJECT_SUCCESS));
    }
}