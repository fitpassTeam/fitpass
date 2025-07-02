package org.example.fitpass.domain.membership.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.fitpass.domain.membership.entity.Membership;

@Schema(description = "이용권 정보 응답 DTO")
public record MembershipResponseDto (
    @Schema(description = "이용권 ID", example = "1")
    Long id,
    
    @Schema(description = "이용권 이름", example = "1개월 자유 이용권")
    String name,
    
    @Schema(description = "이용권 가격 (원 단위)", example = "80000")
    int price,
    
    @Schema(description = "이용권 상세 정보", example = "헬스장 자유 이용 가능, 샤워실 이용 포함, 주차 2시간 무료")
    String content,
    
    @Schema(description = "이용 가능 일수", example = "30")
    int durationInDays
) {
    public static MembershipResponseDto fromEntity(long id, String name, int price, String content, int durationInDays) {
        return new MembershipResponseDto(id, name, price, content, durationInDays);
    }

    public static MembershipResponseDto of(Membership membership){
        return new MembershipResponseDto(
        membership.getId(),
        membership.getName(),
        membership.getPrice(),
        membership.getContent(),
        membership.getDurationInDays());
    }

}
