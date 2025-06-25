package org.example.fitpass.domain.user.dto;

import lombok.Getter;

@Getter
public class PasswordCheckRequestDto {
    private final String password;

    public PasswordCheckRequestDto(String password) {
        this.password = password;
    }
}
