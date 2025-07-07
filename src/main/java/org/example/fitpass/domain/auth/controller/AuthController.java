package org.example.fitpass.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.user.dto.request.LoginRequestDto;
import org.example.fitpass.domain.user.dto.request.LogoutRequestDto;
import org.example.fitpass.domain.user.dto.request.RefreshTokenRequestDto;
import org.example.fitpass.domain.user.dto.request.UserRequestDto;
import org.example.fitpass.domain.user.dto.response.SigninResponseDto;
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
@Tag(name = "Auth API", description = "회원가입, 로그인, 로그아웃, 토큰 관리")
public class AuthController {

    private final UserService userService;

    // 회원가입
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
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
            request.gender()
        );
        return ResponseEntity.status(SuccessCode.SIGNUP_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.SIGNUP_SUCCESS, response));
    }

    // 로그인
    @Operation(
        summary = "🔑 로그인",
        description = "이메일과 비밀번호로 로그인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<SigninResponseDto>> login(
        @RequestBody LoginRequestDto request
    ) {
        SigninResponseDto responseDto = userService.login(request.email(), request.password());
        return ResponseEntity.status(SuccessCode.LOGIN_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.LOGIN_SUCCESS, responseDto));
    }

    // 로그아웃
    @Operation(
        summary = "로그아웃",
        description = "현재 로그인된 사용자를 로그아웃합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/logout")
    public ResponseEntity<ResponseMessage<Void>> logout(
        @RequestHeader("Authorization") String bearerToken,
        @RequestBody LogoutRequestDto request
    ) {
        userService.logout(request.email(), bearerToken);
        return ResponseEntity.status(SuccessCode.LOGOUT_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.LOGOUT_SUCCESS));
    }

    // 토큰 재발급
    @Operation(
        summary = "토큰 재발급",
        description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/reissue")
    public ResponseEntity<ResponseMessage<SigninResponseDto>> reissueToken(
        @RequestBody RefreshTokenRequestDto request
    ) {
        SigninResponseDto response = userService.reissueToken(request.refreshToken());
        return ResponseEntity.status(SuccessCode.REISSUE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.REISSUE_SUCCESS, response));
    }
}


