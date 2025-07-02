package org.example.fitpass.domain.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "체육관 등록 요청 DTO")
public record GymRequestDto(

    @Schema(description = "체육관 이름", example = "헬스핏짐")
    @NotBlank(message = "체육관의 이름을 입력해주세요.")
    @Size(min = 1, max = 20, message = "이름은 최소 1글자에서 20글자내로 입력해주세요.")
    String name,

    @Schema(description = "전화번호 (형식: 0100-1234-5678)", example = "0100-1234-5678")
    @NotBlank(message = "체육관의 번호를 입력해주세요.")
    @Pattern(
        regexp = "^\\d{2,4}-\\d{3,4}-\\d{4}$",
        message = "전화번호 형식이 올바르지 않습니다. 예) 0100-1234-5678"
    )
    String number,

    @Schema(description = "체육관 소개", example = "최신 장비와 다양한 프로그램이 있는 체육관입니다.")
    @NotBlank(message = "체육관 소개를 적어주세요.")
    @Size(max = 500, message = "체육관 소개는 500글자 내로 입력해주세요.")
    String content,

    @Schema(description = "시/도", example = "서울특별시")
    @NotBlank(message = "시/도를 기입해주세요.")
    String city,

    @Schema(description = "구/군", example = "강남구")
    @NotBlank(message = "구/시를 기입해주세요.")
    String district,

    @Schema(description = "상세 주소", example = "테헤란로 123 4층")
    @NotBlank(message = "체육관의 주소를 기입해주세요.")
    String detailAddress,

    @Schema(description = "영업 시작 시간", example = "06:00")
    @NotNull(message = "오픈시간을 입력해주세요.")
    LocalTime openTime,

    @Schema(description = "영업 종료 시간", example = "23:00")
    @NotNull(message = "영업종료시간을 입력해주세요.")
    LocalTime closeTime,

    @Schema(description = "체육관 이미지 URL 목록", example = "[\"https://s3.amazonaws.com/.../image1.jpg\"]")
    List<String> gymImage,

    @Schema(description = "한 줄 소개", example = "강남 최고의 피트니스 센터")
    String summary

) {}