package org.example.fitpass.domain.auth.dto.response;

public record SigninResponseDto(
    Long userId,
    String accessToken,
    String refreshToken,
    String email
) {

}
