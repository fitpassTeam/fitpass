package org.example.fitpass.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.user.dto.request.UpdatePasswordRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePhoneRequestDto;
import org.example.fitpass.domain.user.dto.request.UserInfoUpdateRequestDto;
import org.example.fitpass.domain.user.dto.request.UserRequestDto;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.example.fitpass.common.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

}
