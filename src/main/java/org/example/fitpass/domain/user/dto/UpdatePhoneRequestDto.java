package org.example.fitpass.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class UpdatePhoneRequestDto {
    private final String phone;

    public UpdatePhoneRequestDto(String phone) {
        this.phone = phone;
    }
}
