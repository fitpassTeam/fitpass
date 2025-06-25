package org.example.fitpass.domain.membership.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.membership.dto.request.MembershipRequestDto;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.service.MembershipService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<ResponseMessage<MembershipResponseDto>> createMembership(
        @PathVariable("gymId") Long gymId,
        @Valid @RequestBody MembershipRequestDto dto) {
        MembershipResponseDto response = membershipService.createMembership(
            gymId,
            dto.name(),
            dto.price(),
            dto.content(),
            dto.durationInDays()
        );
        return ResponseEntity.status(SuccessCode.POST_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POST_MEMBERSHIP_SUCCESS, response));
    }

    @GetMapping
    public ResponseEntity<ResponseMessage<List<MembershipResponseDto>>> getAllMemberships(
        @PathVariable("gymId") Long gymId) {
        List<MembershipResponseDto> response = membershipService.getAllByGym(gymId);
        return ResponseEntity.status(SuccessCode.GET_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_MEMBERSHIP_SUCCESS, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<MembershipResponseDto>> getMembershipById(
        @PathVariable("gymId") Long gymId,
        @PathVariable("id") Long id) {
        MembershipResponseDto response = membershipService.getById(gymId, id);
        return ResponseEntity.status(SuccessCode.GET_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_MEMBERSHIP_SUCCESS, response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseMessage<MembershipResponseDto>> updateMembership(
        @PathVariable("gymId") Long gymId,
        @PathVariable("id") Long id,
        @Valid @RequestBody MembershipRequestDto dto) {
        MembershipResponseDto response = membershipService.updateMembership(
            gymId,
            id,
            dto.name(),
            dto.price(),
            dto.content(),
            dto.durationInDays()
        );
        return ResponseEntity.status(SuccessCode.PATCH_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.PATCH_MEMBERSHIP_SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage<Void>> deleteMembership(
        @PathVariable("gymId") Long gymId,
        @PathVariable("id") Long id) {
        membershipService.deleteMembership(gymId, id);
        return ResponseEntity.status(SuccessCode.DELETE_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.DELETE_MEMBERSHIP_SUCCESS));
    }

}
