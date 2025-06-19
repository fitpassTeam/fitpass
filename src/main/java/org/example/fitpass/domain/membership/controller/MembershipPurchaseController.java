package org.example.fitpass.domain.membership.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.membership.dto.response.MembershipPurchaseResponseDto;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.service.MembershipPurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gyms/{gymId}/memberships/")
public class MembershipPurchaseController {

    private final MembershipPurchaseService membershipPurchaseService;

    // 이용권 구매
    @PostMapping("/{membershipId}/purchase")
    public ResponseEntity<ResponseMessage<MembershipPurchaseResponseDto>> purchase(
        @PathVariable("membershipId") Long membershipId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        MembershipPurchaseResponseDto response = membershipPurchaseService.purchase(membershipId,
            userDetails.getId());
        ResponseMessage<MembershipPurchaseResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.PURCHASE_MEMBERSHIP_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.PURCHASE_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    // 구매 이력 조회
    @GetMapping("/purchases")
    public ResponseEntity<ResponseMessage<List<MembershipPurchaseResponseDto>>> getMyPurchases(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MembershipPurchaseResponseDto> response =
            membershipPurchaseService.getMyPurchases(userDetails.getId());
        ResponseMessage<List<MembershipPurchaseResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.GET_MY_MEMBERSHIP_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.GET_MY_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    // 현재 활성 이용권
    @GetMapping("/purchases/active")
    public ResponseEntity<ResponseMessage<MembershipPurchaseResponseDto>> getMyActiveMembership(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MembershipPurchaseResponseDto response = membershipPurchaseService.getMyActive(
            userDetails.getId());
        ResponseMessage<MembershipPurchaseResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.GET_ACTIVE_MEMBERSHIP_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.GET_ACTIVE_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }
}
