package org.example.fitpass.domain.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.domain.gym.entity.Gym;

@Schema(description = "체육관 수정/상세 응답 DTO")
public record GymResDto(

    @Schema(description = "체육관 이름", example = "피트니스존 강남점")
    String name,

    @Schema(description = "전화번호", example = "02-1234-5678")
    String number,

    @Schema(description = "체육관 소개", example = "서울 강남에 위치한 프리미엄 헬스장입니다.")
    String content,

    @Schema(description = "전체 주소 (시/도 + 구/시 + 상세주소)", example = "서울특별시 강남구 테헤란로 123 4층")
    String address,

    @Schema(description = "영업 시작 시간", example = "06:00")
    LocalTime openTime,

    @Schema(description = "영업 종료 시간", example = "22:00")
    LocalTime closeTime,

    @Schema(description = "체육관 ID", example = "10")
    Long gymId,

    @Schema(description = "체육관 이미지 URL 목록", example = "[\"https://fitpass-bucket.s3.ap-northeast-2.amazonaws.com/gym1.jpg\"]")
    List<String> gymImage,

    @Schema(description = "한 줄 요약", example = "강남 최대 규모의 피트니스 센터")
    String summary

) {

    public static GymResDto of(String name, String number, String content, String address,
        LocalTime openTime, LocalTime closeTime, Long gymId, String summary) {
        return new GymResDto(name, number, content, address, openTime, closeTime, gymId, List.of(), summary);
    }

    public static GymResDto from(Gym gym) {
        return new GymResDto(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getFullAddress(),
            gym.getOpenTime(),
            gym.getCloseTime(),
            gym.getId(),
            gym.getImages().stream()
                .map(Image::getUrl)
                .toList(),
            gym.getSummary()
        );
    }
}