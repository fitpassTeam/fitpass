package org.example.fitpass.domain.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.Image;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class GymRequestDto {
    @NotBlank(message = "체육관의 이름을 입력해주세요.")
    @Size(min=1, max = 20, message = "이름은 최소 1글자에서 20글자내로 입력해주세요.")
    private String name;
    @NotBlank(message = "체육관의 번호를 입력해주세요.")
    @Pattern(
        regexp = "^\\d{2,4}-\\d{3,4}-\\d{4}$",
        message = "전화번호 형식이 올바르지 않습니다. 예) 0100-1234-5678"
    )
    private String number;
    @NotBlank(message = "체육관 소개를 적어주세요.")
    @Size(max = 500, message = "체육관 소개는 500글자 내로 입력해주세요.")
    private String content;
    @NotBlank(message = "체육관의 주소를 기입해주세요.")
    private String address;
    @NotNull(message = "오픈시간을 입력해주세요.")
    private LocalTime openTime;
    @NotNull(message = "영업종료시간을 입력해주세요")
    private LocalTime closeTime;
    private List<Image> gymImage; // s3 적용시 변경예정
}
