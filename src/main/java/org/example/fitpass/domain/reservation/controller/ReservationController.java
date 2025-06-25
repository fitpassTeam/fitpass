package org.example.fitpass.domain.reservation.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.reservation.dto.response.GetReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.request.ReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.response.ReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.TrainerReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.request.UpdateReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.response.UpdateReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.UserReservationResponseDto;
import org.example.fitpass.domain.reservation.service.ReservationService;
import org.example.fitpass.common.security.CustomUserDetails;
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
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 가능 시간 조회
    @GetMapping("/gyms/{gymId}/trainers/{trainerId}/available-times")
    public ResponseEntity<ResponseMessage<List<LocalTime>>> getAvailableTimes(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate date) {

        List<LocalTime> availableTimes = reservationService.getAvailableTimes(user.getId(), gymId, trainerId, date);

        return ResponseEntity.status(SuccessCode.AVAILABLE_TIMES_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.AVAILABLE_TIMES_GET_SUCCESS, availableTimes));
    }


    // 예약 생성
    @PostMapping("/gyms/{gymId}/trainers/{trainerId}/reservations")
    public ResponseEntity<ResponseMessage<ReservationResponseDto>> createReservation (
        @Valid @RequestBody ReservationRequestDto reservationRequestDto,
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long gymId,
        @PathVariable Long trainerId) {
        ReservationResponseDto reservationResponseDto =
            reservationService.createReservation(
                reservationRequestDto.reservationDate(),
                reservationRequestDto.reservationTime(),
                user.getId(),
                gymId,
                trainerId);

        return ResponseEntity.status(SuccessCode.RESERVATION_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_CREATE_SUCCESS, reservationResponseDto));
    }

    // 예약 수정
    @PatchMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<UpdateReservationResponseDto>> updateReservation (
        @Valid @RequestBody UpdateReservationRequestDto updateReservationRequestDto,
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ){
        UpdateReservationResponseDto updateReservationResponseDto =
            reservationService.updateReservation(
                updateReservationRequestDto.reservationDate(),
                updateReservationRequestDto.reservationTime(),
                updateReservationRequestDto.reservationStatus(),
                user.getId(),
                gymId,
                trainerId,
                reservationId);

        return ResponseEntity.status(SuccessCode.RESERVATION_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_UPDATE_SUCCESS, updateReservationResponseDto));
    }

    // 예약 취소
    @DeleteMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<Void>> cancelReservation (
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(user.getId(), gymId, trainerId, reservationId);

        return ResponseEntity.status(SuccessCode.RESERVATION_CANCEL_WITH_REFUND_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_CANCEL_WITH_REFUND_SUCCESS));
    }

    // 트레이너별 예약 목록 조회
    @GetMapping("/gyms/{gymId}/trainers/{trainerId}/reservations")
    public ResponseEntity<ResponseMessage<List<TrainerReservationResponseDto>>> getTrainerReservation (
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long gymId,
        @PathVariable Long trainerId
    ) {
        List<TrainerReservationResponseDto> trainerReservationResponseDto =
            reservationService.getTrainerReservation(user.getId(), gymId, trainerId);

        return ResponseEntity.status(SuccessCode.TRAINER_RESERVATION_LIST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.TRAINER_RESERVATION_LIST_SUCCESS, trainerReservationResponseDto));
    }

    // 유저별 예약 목록
    @GetMapping("/users/reservations")
    public ResponseEntity<ResponseMessage<List<UserReservationResponseDto>>> getUserReservations(
        @AuthenticationPrincipal CustomUserDetails user) {
        
        List<UserReservationResponseDto> userReservations =
            reservationService.getUserReservations(user.getId());
        
        return ResponseEntity.status(SuccessCode.USER_RESERVATION_LIST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_RESERVATION_LIST_SUCCESS, userReservations));
    }

    // 예약 단건 조회
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<GetReservationResponseDto>> getReservation(
        @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long reservationId) {

        GetReservationResponseDto reservation =
            reservationService.getReservation(user.getId(), reservationId);
        
        return ResponseEntity.status(SuccessCode.RESERVATION_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_GET_SUCCESS, reservation));
    }

    // 트레이너 예약 승인
    @PatchMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}/confirm")
    public ResponseEntity<ResponseMessage<Void>> confirmReservation(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ) {
        reservationService.confirmReservation(user.getId(), gymId, trainerId, reservationId);

        return ResponseEntity.status(SuccessCode.RESERVATION_CONFIRM_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_CONFIRM_SUCCESS));
    }

    // 트레이너 예약 거부
    @PatchMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}/reject")
    public ResponseEntity<ResponseMessage<Void>> rejectReservation(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ) {
        reservationService.rejectReservation(user.getId(), gymId, trainerId, reservationId);

        return ResponseEntity.status(SuccessCode.RESERVATION_REJECT_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.RESERVATION_REJECT_SUCCESS));
    }
}
