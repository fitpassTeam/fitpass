package org.example.fitpass.domain.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

@Schema(description = "사용자 예약 응답 DTO")
public record UserReservationResponseDto(
    @Schema(description = "예약 ID", example = "1")
    Long reservationId,
    
    @Schema(description = "예약 날짜", example = "2024-12-25")
    LocalDate reservationDate,
    
    @Schema(description = "예약 시간", example = "14:00")
    LocalTime reservationTime,
    
    @Schema(description = "예약 상태", example = "CONFIRMED")
    ReservationStatus status,
    
    @Schema(description = "예약 생성 시간", example = "2024-12-01T10:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "체육관 정보")
    GymInfo gym,
    
    @Schema(description = "트레이너 정보")
    TrainerInfo trainer) {

    @Schema(description = "체육관 정보")
    public record GymInfo(
        @Schema(description = "체육관 ID", example = "1")
        Long gymId,
        
        @Schema(description = "체육관 이름", example = "헬스핏짐")
        String name,
        
        @Schema(description = "체육관 주소", example = "서울특별시 강남구 테헤란로 123")
        String address,
        
        @Schema(description = "체육관 전화번호", example = "02-1234-5678")
        String number) {

    }

    @Schema(description = "트레이너 정보")
    public record TrainerInfo(
        @Schema(description = "트레이너 ID", example = "1")
        Long trainerId,
        
        @Schema(description = "트레이너 이름", example = "김트레이너")
        String name,
        
        @Schema(description = "트레이너 소개", example = "10년 경력의 전문 트레이너입니다.")
        String content,
        
        @Schema(description = "PT 가격", example = "50000")
        int price) {

    }

    // Entity -> Dto 변환 메서드
    public static UserReservationResponseDto from(Reservation reservation) {
        GymInfo gymInfo = new GymInfo(
            reservation.getGym().getId(),
            reservation.getGym().getName(),
            reservation.getGym().getFullAddress(),
            reservation.getGym().getNumber()
        );

        TrainerInfo trainerInfo = new TrainerInfo(
            reservation.getTrainer().getId(),
            reservation.getTrainer().getName(),
            reservation.getTrainer().getContent(),
            reservation.getTrainer().getPrice()
        );

        return new UserReservationResponseDto(
            reservation.getId(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getReservationStatus(),
            reservation.getCreatedAt(),
            gymInfo,
            trainerInfo
        );
    }

}
