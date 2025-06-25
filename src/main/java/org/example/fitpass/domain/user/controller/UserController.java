package org.example.fitpass.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.user.dto.PasswordCheckRequestDto;
import org.example.fitpass.domain.user.dto.UpdatePasswordRequestDto;
import org.example.fitpass.domain.user.dto.UpdatePhoneRequestDto;
import org.example.fitpass.domain.user.dto.UserRequestDto;
import org.example.fitpass.domain.user.dto.UserResponseDto;
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
    public ResponseEntity<ResponseMessage<UserResponseDto>> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDto response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.USER_GET_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_GET_SUCCESS, response));
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<ResponseMessage<UserResponseDto>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequestDto request) {
        UserResponseDto response = userService.updateUserInfo(userDetails.getUsername(), request);
        return ResponseEntity.status(SuccessCode.USER_UPDATE_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_UPDATE_SUCCESS, response));
    }

    // 전화번호 수정
    @PatchMapping("/me/phone")
    public ResponseEntity<ResponseMessage<UserResponseDto>> updatePhone(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePhoneRequestDto request) {
        UserResponseDto response = userService.updatePhone(userDetails.getUsername(), request.getPhone());
        return ResponseEntity.status(SuccessCode.USER_PHONE_EDIT_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_PHONE_EDIT_SUCCESS, response));
    }

    // 비밀번호 수정
    @PatchMapping("/me/password")
    public ResponseEntity<ResponseMessage<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequestDto request) {
        userService.updatePassword(userDetails.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.status(SuccessCode.USER_PASSWORD_EDIT_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_PASSWORD_EDIT_SUCCESS));
    }

}
