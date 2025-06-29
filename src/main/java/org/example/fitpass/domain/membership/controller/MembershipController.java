package org.example.fitpass.domain.membership.controller;

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
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // 이용권 생성
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
    // 모든 이용권 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<List<MembershipResponseDto>>> getAllMemberships(
        @PathVariable("gymId") Long gymId
    ) {
        List<MembershipResponseDto> response = membershipService.getAllByGym(gymId);
        return ResponseEntity.status(SuccessCode.GET_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_MEMBERSHIP_SUCCESS, response));
    }
    // 이용권 상세 조회
    @GetMapping("/{membershipId}")
    public ResponseEntity<ResponseMessage<MembershipResponseDto>> getMembershipById(
        @PathVariable("gymId") Long gymId,
        @PathVariable("membershipId") Long membershipId
    ) {
        MembershipResponseDto response = membershipService.getMembershipById(gymId, membershipId);
        return ResponseEntity.status(SuccessCode.GET_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_MEMBERSHIP_SUCCESS, response));
    }
    // 이용권 수정
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
    // 이용권 삭제
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
