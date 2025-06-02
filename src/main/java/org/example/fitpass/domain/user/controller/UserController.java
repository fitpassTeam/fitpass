package org.example.fitpass.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.user.dto.UserRequestDto;
import org.example.fitpass.domain.user.dto.UserResponseDto;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.service.UserService;
import org.example.fitpass.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto requestDto) {
        return ResponseEntity.ok(userService.signup(requestDto));
    }

    // 로그인 → 토큰 발급
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDto requestDto) {
        User user = userService.login(requestDto);

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getUserRole().name());

        return ResponseEntity.ok()
                .body(new LoginResponse(token, UserResponseDto.from(user)));
    }

    // 사용자 정보 조회
    @GetMapping("/info")
    public ResponseEntity<UserResponseDto> getUserInfo(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserInfo(email));
    }

    // 사용자 정보 수정
    @PutMapping("/info")
    public ResponseEntity<UserResponseDto> updateUserInfo(@RequestParam String email,
                                                          @RequestBody UserRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUserInfo(email, requestDto));
    }

    // ✅ 로그인 응답 DTO
    private record LoginResponse(String token, UserResponseDto user) {}
}
