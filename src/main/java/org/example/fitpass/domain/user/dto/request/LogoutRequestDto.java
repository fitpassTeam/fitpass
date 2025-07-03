package org.example.fitpass.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 요청 DTO")
public record LogoutRequestDto(
    @Schema(description = "사용자 이메일", example = "user@example.com")
    String email
) {

}
