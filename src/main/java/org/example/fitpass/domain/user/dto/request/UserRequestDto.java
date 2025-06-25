package org.example.fitpass.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.fitpass.domain.user.enums.Gender;

public record UserRequestDto(
    @Email(message = "유효한 이메일을 입력해주세요")
    @NotBlank(message = "이메일은 필수입니다")
    String email,

    String userImage,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "비밀번호는 최소 8자이며, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.")
    String password,

    @NotBlank(message = "이름을 입력해주세요")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해주세요")
    String name,

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다")
    String phone,

    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    int age,

    @NotBlank(message = "주소를 입력해주세요")
    String address,

    @NotNull(message = "성별을 선택해주세요")
    Gender gender

) {

    public UserRequestDto(String email, String userImage, String password, String name,
        String phone, int age,
        String address, Gender gender) {
        this.email = email;
        this.userImage = userImage;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.age = age;
        this.address = address;
        this.gender = gender;
    }
}