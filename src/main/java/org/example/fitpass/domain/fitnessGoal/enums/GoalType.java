package org.example.fitpass.domain.fitnessGoal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoalType {
    WEIGHT_LOSS("체중 감량", "kg"),
    WEIGHT_GAIN("체중 증가", "kg"),
    WEIGHT_MAINTAIN("체중 유지", "kg");

    private final String description;
    private final String unit;
}
