package org.example.fitpass.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;

@Getter
@NoArgsConstructor
public class UserRequestDto {

    private String email;
    private String password;
    private String name;
    private String phone;
    private int age;
    private String address;
    private Gender gender;
    private UserRole userRole;

    public void updateInfo(UserRequestDto dto) {
        this.name = dto.getName();
        this.phone = dto.getPhone();
        this.age = dto.getAge();
        this.address = dto.getAddress();
        this.gender = dto.getGender();
        this.userRole = dto.getUserRole();
    }

}