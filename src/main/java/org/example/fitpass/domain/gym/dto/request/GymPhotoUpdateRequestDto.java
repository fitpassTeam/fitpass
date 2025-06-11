package org.example.fitpass.domain.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class GymPhotoUpdateRequestDto {
    @NotNull(message = "사진을 골라주세요.")
    private final List<String> photoUrls = new ArrayList<>();
}
