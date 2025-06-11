package org.example.fitpass.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;

@Getter
public class UserRequestDto {

    private final String email;
    private final String password;
    private final String name;
    private final String phone;
    private final int age;
    private final String address;
    private final Gender gender;
    private final UserRole userRole;


    public UserRequestDto(String email, String password, String name, String phone, int age,
        String address, Gender gender, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.age = age;
        this.address = address;
        this.gender = gender;
        this.userRole = userRole;
    }
}