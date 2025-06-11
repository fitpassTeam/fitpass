package org.example.fitpass.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class SigninResponseDto {

    private final Long userId;

    private final String accessToken;

    private final String refreshToken;

    private final String email;

    public SigninResponseDto(Long userId, String accessToken, String refreshToken, String email) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
    }
}
