package org.example.fitpass.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 수정 요청 DTO")
public record UserInfoUpdateRequestDto(
    @Schema(description = "이름", example = "김핏패스")
    String name,
    
    @Schema(description = "나이", example = "26")
    int age,
    
    @Schema(description = "주소", example = "서울특별시 강남구 역삼동 123-45")
    String address,
    
    @Schema(description = "전화번호", example = "010-9876-5432")
    String phone,
    
    @Schema(description = "프로필 이미지 URL", example = "https://s3.amazonaws.com/.../new_profile.jpg")
    String userImage
) {

}
