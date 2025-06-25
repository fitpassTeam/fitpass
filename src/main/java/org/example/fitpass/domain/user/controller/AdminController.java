package org.example.fitpass.domain.user.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // Admin용: 승인 대기 목록 조회
    @GetMapping("/admin/pending-owner-requests")
    public ResponseEntity<ResponseMessage<List<UserResponseDto>>> getPendingOwnerRequests() {
        List<UserResponseDto> response = userService.getPendingOwnerRequests();
        return ResponseEntity.ok()
            .body(ResponseMessage.success(SuccessCode.PENDING_REQUESTS_GET_SUCCESS, response));
    }

    // Admin용: 승인
    @PatchMapping("/admin/approve-owner/{userId}")
    public ResponseEntity<ResponseMessage<UserResponseDto>> approveOwnerUpgrade(
        @PathVariable Long userId) {
        UserResponseDto response = userService.approveOwnerUpgrade(userId);
        return ResponseEntity.status(SuccessCode.OWNER_UPGRADE_APPROVE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.OWNER_UPGRADE_APPROVE_SUCCESS, response));
    }

    // Admin용: 거절
    @PatchMapping("/admin/reject-owner/{userId}")
    public ResponseEntity<ResponseMessage<UserResponseDto>> rejectOwnerUpgrade(
        @PathVariable Long userId) {
        UserResponseDto response = userService.rejectOwnerUpgrade(userId);
        return ResponseEntity.status(SuccessCode.OWNER_UPGRADE_REJECT_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.OWNER_UPGRADE_REJECT_SUCCESS, response));
    }
}
