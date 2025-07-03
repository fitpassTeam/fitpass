package org.example.fitpass.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.service.GymService;
import org.example.fitpass.domain.user.dto.request.PasswordCheckRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePasswordRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePhoneRequestDto;
import org.example.fitpass.domain.user.dto.request.UserInfoUpdateRequestDto;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "사용자 관리", description = "사용자 정보 조회 및 수정")
public class UserController {

    private final UserService userService;
    private final GymService gymService;

    // 비밀번호 조회
    @Operation(
        summary = "비밀번호 확인",
        description = "현재 비밀번호가 올바른지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 확인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    })
    @PostMapping("/me/password-check")
    public ResponseEntity<ResponseMessage<Void>> checkPassword(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody PasswordCheckRequestDto dto
    ) {
        userService.checkPassword(userDetails.getPassword(), dto.password());
        return ResponseEntity.status(SuccessCode.PASSWORD_MACTH_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.PASSWORD_MACTH_SUCCESS));
    }

    // 내 정보 조회
    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/me")
    public ResponseEntity<ResponseMessage<UserResponseDto>> me(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponseDto response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.USER_GET_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_GET_SUCCESS, response));
    }

    // 모든 유저 조회
    @Operation(
        summary = "모든 사용자 조회",
        description = "등록된 모든 사용자 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping
    public ResponseEntity<ResponseMessage<List<UserResponseDto>>> getAllUsers(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserResponseDto> response = userService.getAllUser(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.USER_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_GET_SUCCESS, response));
    }

    // 내 정보 수정
    @Operation(
        summary = "내 정보 수정",
        description = "현재 로그인한 사용자의 기본 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me")
    public ResponseEntity<ResponseMessage<UserResponseDto>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserInfoUpdateRequestDto request) {
        UserResponseDto response = userService.updateUserInfo(
            userDetails.getId(),
            request.name(),
            request.age(),
            request.address(),
            request.phone(),
            request.userImage()
            );
        return ResponseEntity.status(SuccessCode.USER_UPDATE_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_UPDATE_SUCCESS, response));
    }

    // 유저 프로필 이미지 업데이트
    @Operation(
        summary = "프로필 이미지 수정",
        description = "사용자의 프로필 이미지를 업데이트합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로필 이미지 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파일 형식"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/profile-image")
    public ResponseEntity<ResponseMessage<String>> updateProfileImage(
        @RequestParam("profileImage") MultipartFile file,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        String updatedImageUrl = userService.updateProfileImage(file, userDetails.getId());
        return ResponseEntity.status(SuccessCode.USER_PROFILE_IMAGE_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_PROFILE_IMAGE_UPDATE_SUCCESS, updatedImageUrl));
    }

    // 전화번호 수정
    @Operation(
        summary = "전화번호 수정",
        description = "사용자의 전화번호를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "전화번호 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 전화번호 형식"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/phone")
    public ResponseEntity<ResponseMessage<UserResponseDto>> updatePhone(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePhoneRequestDto request) {
        UserResponseDto response = userService.updatePhone(userDetails.getUsername(), request.phone());
        return ResponseEntity.status(SuccessCode.USER_PHONE_EDIT_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_PHONE_EDIT_SUCCESS, response));
    }

    // 비밀번호 수정
    @Operation(
        summary = "비밀번호 수정",
        description = "사용자의 비밀번호를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "기존 비밀번호 불일치")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<ResponseMessage<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequestDto request) {
        userService.updatePassword(userDetails.getUsername(), request.oldPassword(), request.newPassword());
        return ResponseEntity.status(SuccessCode.USER_PASSWORD_EDIT_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_PASSWORD_EDIT_SUCCESS));
    }

    // Owner로 전환 신청
    @Operation(
        summary = "오너 전환 신청",
        description = "일반 사용자가 헬스장 오너로 전환을 신청합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "오너 전환 신청 성공"),
        @ApiResponse(responseCode = "400", description = "이미 오너이거나 신청 불가"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/me/upgrade-to-owner")
    public ResponseEntity<ResponseMessage<UserResponseDto>> requestOwnerUpgrade (
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponseDto response = userService.requestOwnerUpgrade(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.OWNER_UPGRADE_REQUEST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.OWNER_UPGRADE_REQUEST_SUCCESS, response));
    }

    // 오너 체육관 조회
    @Operation(
        summary = "‍내 헬스장 목록 조회",
        description = "오너가 소유한 헬스장 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "헬스장 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "오너 권한 없음")
    })
    @GetMapping("/me/gyms")
    public ResponseEntity<ResponseMessage<List<GymResponseDto>>> getAllGyms(
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<GymResponseDto> response = gymService.getAllMyGyms(user.getId());
        return ResponseEntity.status(SuccessCode.GYM_SEARCH_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_SEARCH_SUCCESS, response));
    }

    // 특정 유저 조회
    @Operation(
        summary = "특정 사용자 조회",
        description = "사용자 ID로 특정 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseMessage<UserResponseDto>> getUser(
        @PathVariable Long userId
    ) {
        UserResponseDto response = userService.getUser(userId);
        return ResponseEntity.status(SuccessCode.USER_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_GET_SUCCESS, response));
    }
}