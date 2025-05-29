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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate date) {

        List<LocalTime> availableTimes = reservationService.getAvailableTimes(gymId, trainerId, date);

        ResponseMessage<List<LocalTime>> responseMessage =
            ResponseMessage.success(SuccessCode.AVAILABLE_TIMES_GET_SUCCESS, availableTimes);

        return ResponseEntity.status(SuccessCode.AVAILABLE_TIMES_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }


    // 예약 생성
    @PostMapping("/users/{userId}/gyms/{gymId}/trainers/{trainerId}/reservations")
    public ResponseEntity<ResponseMessage<ReservationResponseDto>> createReservation (
        @Valid @RequestBody ReservationRequestDto reservationRequestDto,
        @PathVariable Long userId,
        @PathVariable Long gymId,
        @PathVariable Long trainerId) {
        ReservationResponseDto reservationResponseDto =
            reservationService.createReservation(reservationRequestDto, userId, gymId, trainerId);

        ResponseMessage<ReservationResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.RESERVATION_CREATE_SUCCESS, reservationResponseDto);

        return ResponseEntity.status(SuccessCode.RESERVATION_CREATE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 예약 수정
    @PatchMapping("/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<UpdateReservationResponseDto>> updateReservation (
        @Valid @RequestBody UpdateReservationRequestDto updateReservationRequestDto,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ){
        UpdateReservationResponseDto updateReservationResponseDto =
            reservationService.updateReservation(updateReservationRequestDto, gymId, trainerId, reservationId);

        ResponseMessage<UpdateReservationResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.RESERVATION_UPDATE_SUCCESS, updateReservationResponseDto);

        return ResponseEntity.status(SuccessCode.RESERVATION_UPDATE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 예약 취소
    @DeleteMapping("/users/{userId}/gyms/{gymId}/trainers/{trainerId}/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<Void>> cancelReservation (
        @PathVariable Long userId,
        @PathVariable Long gymId,
        @PathVariable Long trainerId,
        @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(userId, gymId, trainerId, reservationId);

        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.RESERVATION_CANCEL_WITH_REFUND_SUCCESS);

        return ResponseEntity.status(SuccessCode.RESERVATION_CANCEL_WITH_REFUND_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 트레이너별 예약 목록 조회
    @GetMapping("/gyms/{gymId}/trainers/{trainerId}/reservations")
    public ResponseEntity<ResponseMessage<List<TrainerReservationResponseDto>>> getTrainerReservation (
        @PathVariable Long gymId,
        @PathVariable Long trainerId
    ) {
        List<TrainerReservationResponseDto> trainerReservationResponseDto =
            reservationService.getTrainerReservation(gymId, trainerId);

        ResponseMessage<List<TrainerReservationResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.TRAINER_RESERVATION_LIST_SUCCESS, trainerReservationResponseDto);
        return ResponseEntity.status(SuccessCode.TRAINER_RESERVATION_LIST_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 유저별 예약 목록
    @GetMapping("/users/{userId}/reservations")
    public ResponseEntity<ResponseMessage<List<UserReservationResponseDto>>> getUserReservations(
        @PathVariable Long userId) {
        
        List<UserReservationResponseDto> userReservations =
            reservationService.getUserReservations(userId);
        
        ResponseMessage<List<UserReservationResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.USER_RESERVATION_LIST_SUCCESS, userReservations);
        
        return ResponseEntity.status(SuccessCode.USER_RESERVATION_LIST_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 예약 단건 조회
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ResponseMessage<GetReservationResponseDto>> getReservation(
        @PathVariable Long reservationId) {

        GetReservationResponseDto reservation =
            reservationService.getReservation(reservationId);
        
        ResponseMessage<GetReservationResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.RESERVATION_GET_SUCCESS, reservation);
        
        return ResponseEntity.status(SuccessCode.RESERVATION_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }
}
