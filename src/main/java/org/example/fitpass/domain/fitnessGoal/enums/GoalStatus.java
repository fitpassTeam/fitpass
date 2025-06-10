package org.example.fitpass.domain.fitnessGoal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoalStatus {
    ACTIVE("진행중"),
    COMPLETED("목표달성"),
    EXPIRED("기간만료"),
    CANCELLED("취소됨");

    private final String description;
}
