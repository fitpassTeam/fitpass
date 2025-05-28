package org.example.fitpass.domain.gym.dto.request;

import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GymRequestDto {
    private String gymImage;
    private String name;
    private String number;
    private String content;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
}
