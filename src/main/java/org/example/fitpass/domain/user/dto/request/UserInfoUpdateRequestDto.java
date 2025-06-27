package org.example.fitpass.domain.user.dto.request;

public record UserInfoUpdateRequestDto(
    String name,
    int age,
    String address,
    String phone,
    String userImage
) {

}
