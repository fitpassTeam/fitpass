package org.example.fitpass.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.auth.dto.response.SigninResponseDto;
import org.example.fitpass.domain.user.dto.LoginRequestDto;
import org.example.fitpass.domain.user.dto.LogoutRequestDto;
import org.example.fitpass.domain.user.dto.UserRequestDto;
import org.example.fitpass.domain.user.dto.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage<UserResponseDto>> signup(
            @Valid @RequestBody UserRequestDto request
    ) {
        UserResponseDto response = userService.signup(request);
        return ResponseEntity.status(SuccessCode.SIGNUP_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.SIGNUP_SUCCESS, response));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<SigninResponseDto>> login(@RequestBody LoginRequestDto request) {
        SigninResponseDto responseDto = userService.login(request);
        return ResponseEntity.status(SuccessCode.LOGIN_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.LOGIN_SUCCESS, responseDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody LogoutRequestDto request
    ) {
        userService.logout(request.getEmail(), bearerToken);
        return ResponseEntity.ok().build();
    }
}
