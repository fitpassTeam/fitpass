package org.example.fitpass.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 요청 DTO")
public record UpdatePasswordRequestDto(
    @Schema(description = "현재 비밀번호", example = "OldPassword123!")
    String oldPassword,
    
    @Schema(description = "새로운 비밀번호", example = "NewPassword123!")
    String newPassword
) {

}
