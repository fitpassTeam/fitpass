package org.example.fitpass.domain.user.controller;

import lombok.RequiredArgsConstructor;
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

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDto response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> update(@AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody UserRequestDto request) {
        UserResponseDto response = userService.updateUserInfo(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

}
