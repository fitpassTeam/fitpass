package org.example.fitpass.domain.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.membership.dto.response.MembershipPurchaseResponseDto;
import org.example.fitpass.domain.membership.service.MembershipPurchaseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "MembershipPurchase API", description = "이용권 사용에 대한 설명입니다.")
public class MembershipPurchaseController {

    private final MembershipPurchaseService membershipPurchaseService;

    @Operation(summary = "이용권 구매",
        description = "필요 파라미터 : 체육관 ID, 이용권 ID, 활성화 날짜")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "이용권 구매 성공"),
        @ApiResponse(responseCode = "404", description = "체육관 또는 이용권을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "잘못된 구매 요청"),
        @ApiResponse(responseCode = "401", description = "인증이 필요함")
    })
    @Parameter(name = "gymId", description = "체육관 ID", example = "1")
    @Parameter(name = "membershipId", description = "이용권 ID", example = "1")
    @Parameter(name = "activationDate", description = "이용권 활성화 날짜 (yyyy-MM-dd 형식)", example = "2025-07-10")
    @PostMapping("/gyms/{gymId}/memberships/{membershipId}/purchase")
    public ResponseEntity<ResponseMessage<MembershipPurchaseResponseDto>> purchase(
        @PathVariable("membershipId") Long membershipId,
        @PathVariable("gymId") Long gymId,
        @RequestParam("activationDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate activationDate,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MembershipPurchaseResponseDto response = membershipPurchaseService.purchase(membershipId,
            userDetails.getId(), gymId, activationDate);
        return ResponseEntity.status(SuccessCode.PURCHASE_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.PURCHASE_MEMBERSHIP_SUCCESS, response));
    }

    @Operation(summary = "사용 가능한 이용권 조회",
        description = "유저의 사용 가능한 이용권을 조회하는 기능입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용 가능한 이용권 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증이 필요함")
    })
    @GetMapping("/memberships/purchases/not-started")
    public ResponseEntity<ResponseMessage<List<MembershipPurchaseResponseDto>>> getNotStartedMemberships(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<MembershipPurchaseResponseDto> response =
            membershipPurchaseService.getNotStartedMemberships(userDetails.getId());
        return ResponseEntity.status(SuccessCode.GET_NOT_STARTED_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(
                ResponseMessage.success(SuccessCode.GET_NOT_STARTED_MEMBERSHIP_SUCCESS, response));
    }

    @Operation(summary = "구매 이력 조회",
        description = "유저의 구매한 이용권을 조회하는 기능입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "구매 이력 조회 성공"),
        @ApiResponse(responseCode = "404", description = "활성화된 이용권이 없음"),
        @ApiResponse(responseCode = "401", description = "인증이 필요함")
    })
    @GetMapping("/memberships/purchases/me")
    public ResponseEntity<ResponseMessage<List<MembershipPurchaseResponseDto>>> getMyPurchases(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<MembershipPurchaseResponseDto> response =
            membershipPurchaseService.getMyPurchases(userDetails.getId());
        return ResponseEntity.status(SuccessCode.GET_MY_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_MY_MEMBERSHIP_SUCCESS, response));
    }

    @Operation(summary = "현재 활성 이용권", description = "유저의 현재 활성화 된 이용권을 조회하는 기능입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "활성 이용권 조회 성공"),
        @ApiResponse(responseCode = "404", description = "활성화된 이용권이 없음"),
        @ApiResponse(responseCode = "401", description = "인증이 필요함")
    })
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
