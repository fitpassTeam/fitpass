package org.example.fitpass.domain.user.controller;

import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.service.GymService;
import org.example.fitpass.domain.user.dto.request.PasswordCheckRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePasswordRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePhoneRequestDto;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.service.UserService;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.user.dto.request.UserInfoUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GymService gymService;

    // 비밀번호 조회
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
    @GetMapping("/me")
    public ResponseEntity<ResponseMessage<UserResponseDto>> me(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponseDto response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.USER_GET_SUCCESS.getHttpStatus())
                .body(ResponseMessage.success(SuccessCode.USER_GET_SUCCESS, response));
    }
    // 모든 유저 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<List<UserResponseDto>>> getAllUsers(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserResponseDto> response = userService.getAllUser(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.USER_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_GET_SUCCESS, response));
    }

    // 내 정보 수정
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
    @PatchMapping("/me/profile-image")
    public ResponseEntity<ResponseMessage<String>> updateProfileImage(
        @RequestParam("profileImage") MultipartFile file,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        String updatedImageUrl = userService.updateProfileImage(file, userDetails.getId());
        return ResponseEntity.status(SuccessCode.USER_PROFILE_IMAGE_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_PROFILE_IMAGE_UPDATE_SUCCESS, updatedImageUrl));
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

    // Owner로 전환 신청
    @PostMapping("/me/upgrade-to-owner")
    public ResponseEntity<ResponseMessage<UserResponseDto>> requestOwnerUpgrade (
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponseDto response = userService.requestOwnerUpgrade(userDetails.getUsername());
        return ResponseEntity.status(SuccessCode.OWNER_UPGRADE_REQUEST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.OWNER_UPGRADE_REQUEST_SUCCESS, response));
    }
    // 오너 체육관 조회
    @GetMapping("/me/gyms")
    public ResponseEntity<ResponseMessage<List<GymResponseDto>>> getAllGyms(
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<GymResponseDto> response = gymService.getAllMyGyms(user.getId());
        return ResponseEntity.status(SuccessCode.GYM_SEARCH_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_SEARCH_SUCCESS, response));
    }
    // 특정 유저 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseMessage<UserResponseDto>> getUser(
        @PathVariable Long userId
    ) {
        UserResponseDto response = userService.getUser(userId);
        return ResponseEntity.status(SuccessCode.USER_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.USER_GET_SUCCESS, response));
    }

}
