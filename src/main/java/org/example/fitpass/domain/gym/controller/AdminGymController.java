package org.example.fitpass.domain.gym.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.service.GymService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminGymController {

    private final GymService gymService;

    // Admin용: 체육관 승인 대기 목록 조회
    @GetMapping("/admin/pending-gym-requests")
    public ResponseEntity<ResponseMessage<List<GymResponseDto>>> getPendingGymRequests() {
        List<GymResponseDto> response = gymService.getPendingGymRequests();
        return ResponseEntity.ok()
            .body(ResponseMessage.success(SuccessCode.PENDING_GYM_REQUESTS_GET_SUCCESS, response));
    }

    // Admin용: 체육관 승인
    @PatchMapping("/admin/approve-gym/{gymId}")
    public ResponseEntity<ResponseMessage<GymResponseDto>> approveGym(
        @PathVariable Long gymId) {
        GymResponseDto response = gymService.approveGym(gymId);
        return ResponseEntity.status(SuccessCode.GYM_APPROVE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_APPROVE_SUCCESS, response));
    }

    // Admin용: 체육관 거절
    @PatchMapping("/admin/reject-gym/{gymId}")
    public ResponseEntity<ResponseMessage<GymResponseDto>> rejectGym(
        @PathVariable Long gymId) {
        GymResponseDto response = gymService.rejectGym(gymId);
        return ResponseEntity.status(SuccessCode.GYM_REJECT_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_REJECT_SUCCESS, response));
    }

}
