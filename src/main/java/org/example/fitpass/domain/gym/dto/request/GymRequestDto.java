package org.example.fitpass.domain.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

public record GymRequestDto(
    @NotBlank(message = "체육관의 이름을 입력해주세요.")
    @Size(min = 1, max = 20, message = "이름은 최소 1글자에서 20글자내로 입력해주세요.")
    String name,

    @NotBlank(message = "체육관의 번호를 입력해주세요.")
    @Pattern(
        regexp = "^\\d{2,4}-\\d{3,4}-\\d{4}$",
        message = "전화번호 형식이 올바르지 않습니다. 예) 0100-1234-5678"
    )
    String number,

    @NotBlank(message = "체육관 소개를 적어주세요.")
    @Size(max = 500, message = "체육관 소개는 500글자 내로 입력해주세요.")
    String content,

    @NotBlank(message = "시/도를 기입해주세요.")
    String city,

    @NotBlank(message = "구/시를 기입해주세요.")
    String district,

    @NotBlank(message = "체육관의 주소를 기입해주세요.")
    String detailAddress,

    @NotNull(message = "오픈시간을 입력해주세요.")
    LocalTime openTime,

    @NotNull(message = "영업종료시간을 입력해주세요.")
    LocalTime closeTime,

    List<String> gymImage,

    String summary
) { }
