package org.example.fitpass.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 DTO")
public record LoginRequestDto(
    @Schema(description = "이메일", example = "user@example.com")
    String email,
    
    @Schema(description = "비밀번호", example = "Password123!")
    String password
) {

}

