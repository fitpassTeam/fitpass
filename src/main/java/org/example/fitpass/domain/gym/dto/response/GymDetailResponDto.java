package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.domain.gym.enums.GymStatus;

public record GymDetailResponDto(
    Long ownerId,
    String name,
    String number,
    String content,
    String fullAddress,
    LocalTime openTime,
    LocalTime closeTime,
    List<String> gymImage,
    List<String> trainerNames,
    GymStatus gymStatus,
    Double averageGymRating,
    Integer totalReviewCount
) {

    public static GymDetailResponDto from(Long ownerId, String name, String number, String content,
        String fullAddress, LocalTime openTime, LocalTime closeTime, List<String> gymImage,
        List<String> trainerNames, GymStatus gymStatus, Double averageGymRating, Integer totalReviewCount) {
        return new GymDetailResponDto(
            ownerId,
            name,
            number,
            content,
            fullAddress,
            openTime,
            closeTime,
            gymImage,
            trainerNames,
            gymStatus,
            averageGymRating,
            totalReviewCount
        );
    }
}
