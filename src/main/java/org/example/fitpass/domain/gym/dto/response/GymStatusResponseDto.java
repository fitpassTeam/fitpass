package org.example.fitpass.domain.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import org.example.fitpass.domain.gym.enums.GymPostStatus;

@Schema(description = "체육관 등록 요청 응답 DTO")
public record GymStatusResponseDto(

    @Schema(description = "체육관 이름", example = "핏패스 헬스장")
    String name,

    @Schema(description = "전화번호", example = "010-1234-5678")
    String number,

    @Schema(description = "체육관 소개", example = "최신 장비와 쾌적한 환경을 갖춘 체육관입니다.")
    String content,

    @Schema(description = "전체 주소", example = "서울시 송파구 올림픽로 123")
    String address,

    @Schema(description = "오픈 시간", example = "06:00")
    LocalTime openTime,

    @Schema(description = "마감 시간", example = "23:00")
    LocalTime closeTime,

    @Schema(description = "체육관 ID", example = "3")
    Long gymId,

    @Schema(description = "한 줄 요약", example = "송파 최대 규모의 피트니스 센터")
    String summary,

    @Schema(description = "체육관 등록 상태", example = "PENDING")
    GymPostStatus gymPostStatus

) {

    public static GymStatusResponseDto of(String name, String number, String content, String fullAddress,
        LocalTime openTime, LocalTime closeTime, Long gymId, String summary, GymPostStatus gymPostStatus) {
        return new GymStatusResponseDto(name, number, content, fullAddress, openTime, closeTime, gymId, summary, gymPostStatus);
    }

}