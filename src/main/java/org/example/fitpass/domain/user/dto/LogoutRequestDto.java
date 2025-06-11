package org.example.fitpass.domain.user.dto;

import lombok.Getter;

@Getter
public class LogoutRequestDto {
    private final String email;

    public LogoutRequestDto(String email) {
        this.email = email;
    }

    public  String getEmail() {
        return email;
    }

}
