package org.example.fitpass.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SigninResponseDto {

    private final Long userId;

    private final String bearerToken;

    private final String email;

}
