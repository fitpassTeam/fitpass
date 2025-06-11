package org.example.fitpass.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;

@Getter
public class UserResponseDto {

    private final Long id;
    private final String email;
    private final String name;
    private final String phone;
    private final int age;
    private final String address;
    private final int pointBalance;
    private final String userImage;
    private final Gender gender;
    private final UserRole userRole;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.age = user.getAge();
        this.address = user.getAddress();
        this.pointBalance = user.getPointBalance();
        this.userImage = user.getUserImage();
        this.gender = user.getGender();
        this.userRole = user.getUserRole();
    }
    public static UserResponseDto from(User user) {
        return new UserResponseDto(user);
    }
}
