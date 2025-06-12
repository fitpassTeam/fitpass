package org.example.fitpass.domain.user.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;

@Getter
public class UserRequestDto {

    @Email(message = "유효한 이메일을 입력해주세요")
    @NotBlank(message = "이메일은 필수입니다")
    private final String email;

    private String userImage;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 최소 8자이며, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.")
    private final String password;

    @NotBlank(message = "이름을 입력해주세요")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해주세요")
    private final String name;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다")
    private final String phone;

    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    private final int age;

    @NotBlank(message = "주소를 입력해주세요")
    private final String address;

    @NotNull(message = "성별을 선택해주세요")
    private final Gender gender;

    @NotNull(message = "유저 역할을 선택해주세요")
    private final UserRole userRole;


    public UserRequestDto(String email, String userImage, String password, String name, String phone, int age,
        String address, Gender gender, UserRole userRole) {
        this.email = email;
        this.userImage = userImage;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.age = age;
        this.address = address;
        this.gender = gender;
        this.userRole = userRole;
    }
}