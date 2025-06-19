package org.example.fitpass.domain.membership.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.membership.dto.request.MembershipRequestDto;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.service.MembershipService;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
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
            dto.content()
        );
        ResponseMessage<MembershipResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.POST_MEMBERSHIP_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.POST_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    @GetMapping
    public ResponseEntity<ResponseMessage<List<MembershipResponseDto>>> getAllMemberships(
        @PathVariable("gymId") Long gymId) {
        List<MembershipResponseDto> response = membershipService.getAllByGym(gymId);
        ResponseMessage<List<MembershipResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.GET_MEMBERSHIP_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.GET_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<MembershipResponseDto>> getMembershipById(
        @PathVariable("gymId") Long gymId,
        @PathVariable("id") Long id) {
        MembershipResponseDto response = membershipService.getById(gymId, id);
        ResponseMessage<MembershipResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.GET_MEMBERSHIP_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.GET_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(responseMessage);
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
            dto.content()
        );
        ResponseMessage<MembershipResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.PATCH_MEMBERSHIP_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.PATCH_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage<Void>> deleteMembership(
        @PathVariable("gymId") Long gymId,
        @PathVariable("id") Long id) {
        membershipService.deleteMembership(gymId, id);
        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.DELETE_MEMBERSHIP_SUCCESS);
        return ResponseEntity.status(SuccessCode.DELETE_MEMBERSHIP_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

}
