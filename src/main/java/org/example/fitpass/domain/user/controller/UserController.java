package org.example.fitpass.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.user.dto.request.PasswordCheckRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePasswordRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePhoneRequestDto;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.user.dto.request.UserInfoUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 비밀번호 조회
    @PostMapping("/me/password-check")
    public ResponseEntity<ResponseMessage<Void>> checkPassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody PasswordCheckRequestDto dto) {
        userService.checkPassword(userDetails.getPassword(), dto.getPassword());
        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.PASSWORD_MACTH_SUCCESS);
        return ResponseEntity.status(SuccessCode.LIKE_TOGGLE_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ResponseMessage<UserResponseDto>> me(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponseDto response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.USER_GET_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_GET_SUCCESS, response));
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<ResponseMessage<UserResponseDto>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserInfoUpdateRequestDto request) {
        UserResponseDto response = userService.updateUserInfo(
            userDetails.getId(),
            request.name(),
            request.age(),
            request.address());
        return ResponseEntity.status(SuccessCode.USER_UPDATE_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_UPDATE_SUCCESS, response));
    }

    // 유저 프로필 이미지 업데이트
    @PatchMapping("/me/profile-image")
    public ResponseEntity<ResponseMessage<String>> updateProfileImage(
        @RequestParam("profileImage") MultipartFile file,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        String updatedImageUrl = userService.updateProfileImage(file, userDetails.getId());
        return ResponseEntity.status(SuccessCode.USER_PROFILE_IMAGE_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_PROFILE_IMAGE_UPDATE_SUCCESS, updatedImageUrl));
    }

    // 전화번호 수정
    @PatchMapping("/me/phone")
    public ResponseEntity<ResponseMessage<UserResponseDto>> updatePhone(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePhoneRequestDto request) {
        UserResponseDto response = userService.updatePhone(userDetails.getUsername(), request.phone());
        return ResponseEntity.status(SuccessCode.USER_PHONE_EDIT_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_PHONE_EDIT_SUCCESS, response));
    }

    // 비밀번호 수정
    @PatchMapping("/me/password")
    public ResponseEntity<ResponseMessage<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequestDto request) {
        userService.updatePassword(userDetails.getUsername(), request.oldPassword(), request.newPassword());
        return ResponseEntity.status(SuccessCode.USER_PASSWORD_EDIT_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_PASSWORD_EDIT_SUCCESS));
    }

    // Owner로 전환 신청
    @PostMapping("/me/upgrade-to-owner")
    public ResponseEntity<ResponseMessage<UserResponseDto>> requestOwnerUpgrade (
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponseDto response = userService.requestOwnerUpgrade(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.OWNER_UPGRADE_REQUEST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.OWNER_UPGRADE_REQUEST_SUCCESS, response));
    }

}
