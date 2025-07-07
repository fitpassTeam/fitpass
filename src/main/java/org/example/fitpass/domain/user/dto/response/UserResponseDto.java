package org.example.fitpass.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;

@Schema(description = "사용자 정보 응답 DTO")
public record UserResponseDto(
    @Schema(description = "사용자 ID", example = "1")
    Long userId,
    
    @Schema(description = "이메일", example = "user@example.com")
    String email,
    
    @Schema(description = "이름", example = "김핏패스")
    String name,
    
    @Schema(description = "전화번호", example = "010-1234-5678")
    String phone,
    
    @Schema(description = "나이", example = "25")
    int age,
    
    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    String address,
    
    @Schema(description = "포인트 잔액", example = "10000")
    int pointBalance,
    
    @Schema(description = "프로필 이미지 URL", example = "https://s3.amazonaws.com/.../profile.jpg")
    String userImage,
    
    @Schema(description = "성별", example = "MALE")
    Gender gender,
    
    @Schema(description = "사용자 역할", example = "USER")
    UserRole userRole
) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPhone(),
            user.getAge(),
            user.getAddress(),
            user.getPointBalance(),
            user.getUserImage(),
            user.getGender(),
            user.getUserRole()
        );
    }
}
