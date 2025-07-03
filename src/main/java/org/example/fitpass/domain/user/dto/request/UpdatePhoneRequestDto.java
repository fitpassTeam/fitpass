package org.example.fitpass.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "전화번호 수정 요청 DTO")
public record UpdatePhoneRequestDto(
    @Schema(description = "새로운 전화번호", example = "010-9876-5432")
    String phone
) {

}
