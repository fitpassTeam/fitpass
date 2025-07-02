package org.example.fitpass.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "관리자", description = "관리자 전용 기능")
public class AdminController {

    private final UserService userService;

    // Admin용: 승인 대기 목록 조회
    @Operation(
        summary = "오너 승인 대기 목록 조회",
        description = "오너 전환을 신청한 사용자들의 대기 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "대기 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/admin/pending-owner-requests")
    public ResponseEntity<ResponseMessage<List<UserResponseDto>>> getPendingOwnerRequests() {
        List<UserResponseDto> response = userService.getPendingOwnerRequests();
        return ResponseEntity.ok()
            .body(ResponseMessage.success(SuccessCode.PENDING_REQUESTS_GET_SUCCESS, response));
    }

    // Admin용: 승인
    @Operation(
        summary = "오너 승인",
        description = "오너 전환 신청을 승인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "오너 승인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PatchMapping("/admin/approve-owner/{userId}")
    public ResponseEntity<ResponseMessage<UserResponseDto>> approveOwnerUpgrade(
        @PathVariable Long userId
    ) {
        UserResponseDto response = userService.approveOwnerUpgrade(userId);
        return ResponseEntity.status(SuccessCode.OWNER_UPGRADE_APPROVE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.OWNER_UPGRADE_APPROVE_SUCCESS, response));
    }

    // Admin용: 거절
    @Operation(
        summary = "오너 승인 거절",
        description = "오너 전환 신청을 거절합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "오너 승인 거절 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PatchMapping("/admin/reject-owner/{userId}")
    public ResponseEntity<ResponseMessage<UserResponseDto>> rejectOwnerUpgrade(
        @PathVariable Long userId
    ) {
        UserResponseDto response = userService.rejectOwnerUpgrade(userId);
        return ResponseEntity.status(SuccessCode.OWNER_UPGRADE_REJECT_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.OWNER_UPGRADE_REJECT_SUCCESS, response));
    }
}