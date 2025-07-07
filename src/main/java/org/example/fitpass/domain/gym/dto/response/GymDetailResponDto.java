package org.example.fitpass.domain.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.domain.gym.enums.GymStatus;

@Schema(description = "체육관 상세 정보 응답 DTO")
public record GymDetailResponDto(

    @Schema(description = "체육관 소유자 ID", example = "1")
    Long ownerId,

    @Schema(description = "체육관 이름", example = "헬스핏짐")
    String name,

    @Schema(description = "전화번호", example = "010-1234-5678")
    String number,

    @Schema(description = "체육관 소개", example = "최신 시설과 넓은 공간을 갖춘 체육관입니다.")
    String content,

    @Schema(description = "전체 주소", example = "서울특별시 강남구 테헤란로 123 4층")
    String fullAddress,

    @Schema(description = "영업 시작 시간", example = "06:00")
    LocalTime openTime,

    @Schema(description = "영업 종료 시간", example = "23:00")
    LocalTime closeTime,

    @Schema(description = "체육관 이미지 URL 목록", example = "[\"https://s3.amazonaws.com/.../image1.jpg\"]")
    List<String> gymImage,

    @Schema(description = "소속 트레이너 이름 목록", example = "[\"홍길동\", \"김철수\"]")
    List<String> trainerNames,

    @Schema(description = "체육관 상태", example = "APPROVED")
    GymStatus gymStatus,

    @Schema(description = "평균 평점", example = "4.5")
    Double averageGymRating,

    @Schema(description = "총 리뷰 수", example = "32")
    Integer totalReviewCount

) {
    public static GymDetailResponDto from(
        Long ownerId,
        String name,
        String number,
        String content,
        String fullAddress,
        LocalTime openTime,
        LocalTime closeTime,
        List<String> gymImage,
        List<String> trainerNames,
        GymStatus gymStatus,
        Double averageGymRating,
        Integer totalReviewCount
    ) {
        return new GymDetailResponDto(
            ownerId,
            name,
            number,
            content,
            fullAddress,
            openTime,
            closeTime,
            gymImage,
            trainerNames,
            gymStatus,
            averageGymRating,
            totalReviewCount
        );
    }
}