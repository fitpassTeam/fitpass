package org.example.fitpass.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LoginRequestDto {
    private String email;
    private String password;

    public LoginRequestDto() {} // 🔴 Jackson이 이걸 필요로 함

    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter/Setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

