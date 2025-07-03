package org.example.fitpass.domain.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

@Schema(description = "트레이너 예약 응답 DTO")
public record TrainerReservationResponseDto(
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
    
    @Schema(description = "사용자 정보")
    UserInfo user
) {

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

    @Schema(description = "사용자 정보")
    public record UserInfo(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        
        @Schema(description = "사용자 이름", example = "김핏패스")
        String name,
        
        @Schema(description = "사용자 이메일", example = "user@example.com")
        String email,
        
        @Schema(description = "사용자 전화번호", example = "010-1234-5678")
        String phone) {

    }

    // Entity -> Dto 변환 메서드
    public static TrainerReservationResponseDto from(Reservation reservation) {
        GymInfo gymInfo = new GymInfo(
            reservation.getGym().getId(),
            reservation.getGym().getName(),
            reservation.getGym().getFullAddress(),
            reservation.getGym().getNumber()
        );

        UserInfo userInfo = new UserInfo(
            reservation.getUser().getId(),
            reservation.getUser().getName(),
            reservation.getUser().getEmail(),
            reservation.getUser().getPhone()
        );

        return new TrainerReservationResponseDto(
            reservation.getId(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getReservationStatus(),
            reservation.getCreatedAt(),
            gymInfo,
            userInfo
        );
    }
}
