package org.example.fitpass.domain.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.membership.dto.request.MembershipRequestDto;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.service.MembershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gyms/{gymId}/memberships")
@Tag(name = "Membership API", description = "이용권 관리에 대한 설명입니다.")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @Operation(summary = "이용권 등록",
        description = "필요 파라미터 : 체육관 ID, 이름, 비용, 이용권 정보, 사용 기간")
    @Parameter(name = "gymId", description = "체육관 ID")
    @Parameter(name = "name", description = "이용권 이름")
    @Parameter(name = "price", description = "이용권 비용")
    @Parameter(name = "content", description = "이용권 정보")
    @Parameter(name = "durationInDays", description = "사용 기간")
    @PostMapping
    public ResponseEntity<ResponseMessage<MembershipResponseDto>> createMembership(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("gymId") Long gymId,
        @Valid @RequestBody MembershipRequestDto dto) {
        MembershipResponseDto response = membershipService.createMembership(
            gymId,
            userDetails.getId(),
            dto.name(),
            dto.price(),
            dto.content(),
            dto.durationInDays()
        );
        return ResponseEntity.status(SuccessCode.POST_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POST_MEMBERSHIP_SUCCESS, response));
    }

    @Operation(summary = "모든 이용권 조회", description = "체육관에 생성된 이용권울 전체조회하는 기능입니다.")
    @GetMapping
    public ResponseEntity<ResponseMessage<List<MembershipResponseDto>>> getAllMemberships(
        @PathVariable("gymId") Long gymId
    ) {
        List<MembershipResponseDto> response = membershipService.getAllByGym(gymId);
        return ResponseEntity.status(SuccessCode.GET_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_MEMBERSHIP_SUCCESS, response));
    }

    @Operation(summary = "이용권 상세 조회", description = "체육관에 생성된 이용권울 상세조회하는 기능입니다.")
    @GetMapping("/{membershipId}")
    public ResponseEntity<ResponseMessage<MembershipResponseDto>> getMembershipById(
        @PathVariable("gymId") Long gymId,
        @PathVariable("membershipId") Long membershipId
    ) {
        MembershipResponseDto response = membershipService.getMembershipById(gymId, membershipId);
        return ResponseEntity.status(SuccessCode.GET_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_MEMBERSHIP_SUCCESS, response));
    }

    @Operation(summary = "이용권 정보 수정", description = "체육관에 생성된 이용권의 정보수정하는 기능입니다.")
    @PatchMapping("/{membershipId}")
    public ResponseEntity<ResponseMessage<MembershipResponseDto>> updateMembership(
        @PathVariable("gymId") Long gymId,
        @PathVariable("membershipId") Long membershipId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody MembershipRequestDto dto
    ) {
        MembershipResponseDto response = membershipService.updateMembership(
            gymId,
            membershipId,
            userDetails.getId(),
            dto.name(),
            dto.price(),
            dto.content(),
            dto.durationInDays()
        );
        return ResponseEntity.status(SuccessCode.PATCH_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.PATCH_MEMBERSHIP_SUCCESS, response));
    }

    @Operation(summary = "이용권 삭제", description = "체육관에 생성된 이용권울 삭제하는 기능입니다.")
    @DeleteMapping("/{membershipId}")
    public ResponseEntity<ResponseMessage<Void>> deleteMembership(
        @PathVariable("gymId") Long gymId,
        @PathVariable("membershipId") Long membershipId,
        @AuthenticationPrincipal CustomUserDetails userDetails
        ) {
        membershipService.deleteMembership(gymId, membershipId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.DELETE_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.DELETE_MEMBERSHIP_SUCCESS));
    }

}
