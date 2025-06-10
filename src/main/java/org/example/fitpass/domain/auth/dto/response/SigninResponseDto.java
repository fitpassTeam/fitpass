package org.example.fitpass.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SigninResponseDto {

    private final Long userId;

    private String accessToken;

    private String refreshToken;

    private final String email;

}
