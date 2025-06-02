package org.example.fitpass.jwt;

import lombok.RequiredArgsConstructor;

import org.example.fitpass.domain.user.dto.UserRequestDto;
import org.example.fitpass.domain.user.dto.UserResponseDto;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class JwtAuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody UserRequestDto request) {
        UserResponseDto response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserRequestDto request) {
        User user = userService.login(request);
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getUserRole().name());
        return ResponseEntity.ok(token);
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        UserResponseDto response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> update(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails,
                                                  @RequestBody UserRequestDto request) {
        UserResponseDto response = userService.updateUserInfo(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
