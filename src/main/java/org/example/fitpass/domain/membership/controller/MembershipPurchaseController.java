package org.example.fitpass.domain.membership.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.membership.dto.response.MembershipPurchaseResponseDto;
import org.example.fitpass.domain.membership.service.MembershipPurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MembershipPurchaseController {

    private final MembershipPurchaseService membershipPurchaseService;

    // 이용권 구매
    @PostMapping("/gyms/{gymId}/memberships/{membershipId}/purchase")
    public ResponseEntity<ResponseMessage<MembershipPurchaseResponseDto>> purchase(
        @PathVariable("membershipId") Long membershipId,
        @PathVariable("gymId") Long gymId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        MembershipPurchaseResponseDto response = membershipPurchaseService.purchase(membershipId,
            userDetails.getId(), gymId);
        return ResponseEntity.status(SuccessCode.PURCHASE_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.PURCHASE_MEMBERSHIP_SUCCESS, response));
    }

    // 이용권 사용
    @PostMapping("/memberships/purchases/{purchaseId}/start")
    public ResponseEntity<ResponseMessage<MembershipPurchaseResponseDto>> startMembership(
        @PathVariable("purchaseId") Long purchaseId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        MembershipPurchaseResponseDto response = membershipPurchaseService.startMembership(purchaseId,
            userDetails.getId());
        return ResponseEntity.status(SuccessCode.START_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.START_MEMBERSHIP_SUCCESS, response));
    }

    // 사용 가능한 이용권 조회
    @GetMapping("/memberships/purchases/not-started")
    public ResponseEntity<ResponseMessage<List<MembershipPurchaseResponseDto>>> getNotStartedMemberships(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MembershipPurchaseResponseDto> response =
            membershipPurchaseService.getNotStartedMemberships(userDetails.getId());
        return ResponseEntity.status(SuccessCode.GET_NOT_STARTED_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_NOT_STARTED_MEMBERSHIP_SUCCESS, response));
    }

    // 구매 이력 조회
    @GetMapping("/memberships/purchases/me")
    public ResponseEntity<ResponseMessage<List<MembershipPurchaseResponseDto>>> getMyPurchases(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MembershipPurchaseResponseDto> response =
            membershipPurchaseService.getMyPurchases(userDetails.getId());
        return ResponseEntity.status(SuccessCode.GET_MY_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_MY_MEMBERSHIP_SUCCESS, response));
    }

    // 현재 활성 이용권
    @GetMapping("/memberships/purchases/active")
    public ResponseEntity<ResponseMessage<MembershipPurchaseResponseDto>> getMyActiveMembership(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MembershipPurchaseResponseDto response = membershipPurchaseService.getMyActive(
            userDetails.getId());
        return ResponseEntity.status(SuccessCode.GET_ACTIVE_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ACTIVE_MEMBERSHIP_SUCCESS, response));
    }
}
