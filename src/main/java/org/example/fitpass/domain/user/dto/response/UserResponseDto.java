package org.example.fitpass.domain.user.dto.response;

import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;

public record UserResponseDto(
    Long id,
    String email,
    String name,
    String phone,
    int age,
    String address,
    int pointBalance,
    String userImage,
    Gender gender,
    UserRole userRole
) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPhone(),
            user.getAge(),
            user.getAddress(),
            user.getPointBalance(),
            user.getUserImage(),
            user.getGender(),
            user.getUserRole()
        );
    }
}
