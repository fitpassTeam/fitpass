package org.example.fitpass.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class UpdatePasswordRequestDto {
    private final String oldPassword;
    private final String newPassword;

    public UpdatePasswordRequestDto(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
