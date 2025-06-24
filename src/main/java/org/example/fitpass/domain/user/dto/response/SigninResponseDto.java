package org.example.fitpass.domain.user.dto.response;

public record SigninResponseDto(
    Long userId,
    String accessToken,
    String refreshToken,
    String email
) {

}
