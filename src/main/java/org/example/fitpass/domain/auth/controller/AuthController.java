package org.example.fitpass.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.user.dto.request.RefreshTokenRequestDto;
import org.example.fitpass.domain.user.dto.response.SigninResponseDto;
import org.example.fitpass.domain.user.dto.request.LoginRequestDto;
import org.example.fitpass.domain.user.dto.request.LogoutRequestDto;
import org.example.fitpass.domain.user.dto.request.UserRequestDto;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        UserResponseDto response = userService.signup(
            request.email(),
            request.userImage(),
            request.password(),
            request.name(),
            request.phone(),
            request.age(),
            request.address(),
            request.gender(),
            request.userRole()
        );
        return ResponseEntity.status(SuccessCode.SIGNUP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.SIGNUP_SUCCESS, response));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<SigninResponseDto>> login(
        @RequestBody LoginRequestDto request) {
        SigninResponseDto responseDto = userService.login(request.email(), request.password());
        return ResponseEntity.status(SuccessCode.LOGIN_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.LOGIN_SUCCESS, responseDto));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestHeader("Authorization") String bearerToken,
        @RequestBody LogoutRequestDto request
    ) {
        userService.logout(request.email(), bearerToken);
        return ResponseEntity.ok().build();
    }
    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ResponseMessage<SigninResponseDto>> reissueToken(
        @RequestBody RefreshTokenRequestDto request
    ) {
        SigninResponseDto response = userService.reissueToken(request.refreshToken());
        return ResponseEntity.status(SuccessCode.REISSUE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.REISSUE_SUCCESS, response));
    }
}


