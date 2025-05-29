package org.example.fitpass.domain.gym.dto.request;

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
    private String name;
    private String number;
    private String content;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private List<Image> gymImage;
}
