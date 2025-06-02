package org.example.fitpass.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;

@Getter
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private int age;
    private String address;
    private int pointBalance;
    private String userImage;
    private Gender gender;
    private UserRole userRole;

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
