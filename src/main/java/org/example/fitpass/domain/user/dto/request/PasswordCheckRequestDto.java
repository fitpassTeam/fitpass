package org.example.fitpass.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 확인 요청 DTO")
public record PasswordCheckRequestDto(
    @Schema(description = "확인할 비밀번호", example = "Password123!")
    String password
) {

}
