package org.example.fitpass.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.auth.dto.response.SigninResponseDto;
import org.example.fitpass.domain.user.dto.LoginRequestDto;
import org.example.fitpass.domain.user.dto.UserRequestDto;
import org.example.fitpass.domain.user.dto.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody UserRequestDto request) {
        UserResponseDto response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<SigninResponseDto> login(@RequestBody LoginRequestDto request) {
        SigninResponseDto responseDto = userService.login(request);
        return ResponseEntity.ok(responseDto);
    }
}
