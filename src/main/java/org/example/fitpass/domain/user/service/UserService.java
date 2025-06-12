package org.example.fitpass.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.redis.RedisService;
import org.example.fitpass.common.service.FileUploadService;
import org.example.fitpass.domain.auth.dto.response.SigninResponseDto;
import org.example.fitpass.domain.user.dto.LoginRequestDto;
import org.example.fitpass.domain.user.dto.UserRequestDto;
import org.example.fitpass.domain.user.dto.UserResponseDto;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final FileUploadService fileUploadService;

    @Transactional
    public UserResponseDto signup(UserRequestDto dto, MultipartFile image) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BaseException(ExceptionCode.USER_ALREADY_EXISTS);
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileUploadService.upload(image); // 로컬 경로 또는 S3 업로드
        }

        User user = new User(
                dto.getEmail(),
                imageUrl,
                passwordEncoder.encode(dto.getPassword()),
                dto.getName(),
                dto.getPhone(),
                dto.getAge(),
                dto.getAddress(),
                dto.getGender(),
                dto.getUserRole()
        );

        userRepository.save(user);
        return UserResponseDto.from(user);
    }


    @Transactional(readOnly = true)
    public SigninResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmailOrElseThrow(dto.getEmail());

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BaseException(ExceptionCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getUserRole().name());

        return new SigninResponseDto(user.getId(), accessToken, refreshToken, user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(String email) {
        User user = userRepository.findByEmailOrElseThrow(email);

        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto updateUserInfo(String email, UserRequestDto dto) {
        User user = userRepository.findByEmailOrElseThrow(email);

        user.updateInfo(dto);
        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto updatePhone(String email, String newPhone) {
        User user = userRepository.findByEmailOrElseThrow(email);

        user.updatePhone(newPhone);
        return UserResponseDto.from(user);
    }

    @Transactional
    public void updatePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmailOrElseThrow(email);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BaseException(ExceptionCode.INVALID_OLD_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public SigninResponseDto reissueToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BaseException(ExceptionCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getUserEmail(jwtTokenProvider.substringToken(refreshToken));

        // Redis에서 refreshToken이 유효한지 확인
        String storedToken = redisService.getRefreshToken(email);
        if (!refreshToken.equals(storedToken)) {
            throw new BaseException(ExceptionCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByEmailOrElseThrow(email);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserRole().name());

        return new SigninResponseDto(user.getId(), newAccessToken, refreshToken, email);
    }

    @Transactional
    public void logout(String email, String bearerToken) {
        String token = jwtTokenProvider.substringToken(bearerToken);
        long remaining = jwtTokenProvider.getRemainingTime(token);
        jwtTokenProvider.blacklistAccessToken(token, remaining);
        redisService.deleteRefreshToken(email);
    }
}