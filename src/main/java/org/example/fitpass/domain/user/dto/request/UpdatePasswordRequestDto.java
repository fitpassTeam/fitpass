package org.example.fitpass.domain.user.dto.request;

public record UpdatePasswordRequestDto(
    String oldPassword,
    String newPassword
) {

}
