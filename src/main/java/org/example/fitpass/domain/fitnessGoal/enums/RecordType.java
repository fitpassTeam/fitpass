package org.example.fitpass.domain.fitnessGoal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecordType {
    EXERCISE_PHOTO("운동 인증 사진"),
    WEIGHT_RECORD("체중 기록");

    private final String description;
}
