package org.example.fitpass.domain.gym.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public record GymPhotoUpdateRequestDto(
    @NotNull(message = "사진을 골라주세요.")
    List<String> photoUrls
) { }