package org.example.fitpass.domain.gym.dto.request;

import java.time.LocalTime;
import lombok.Getter;

@Getter
public class GymUpdateRequestDto {
    private String name;
    private String number;
    private String content;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
}
