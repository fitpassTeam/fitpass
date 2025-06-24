package org.example.fitpass.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.dto.response.SigninResponseDto;
import org.example.fitpass.domain.user.dto.request.LoginRequestDto;
import org.example.fitpass.domain.user.dto.request.UserRequestDto;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    // 회원가입
    @Transactional
    public UserResponseDto signup(
        String email,
        String userImage,
        String password,
        String name,
        String phone,
        int age,
        String address,
        Gender gender,
        UserRole userRole
    ) {
        User user = new User(
                email,
                userImage,
                passwordEncoder.encode(password),
                name,
                phone,
                age,
                address,
                gender,
                userRole
        );
        userRepository.save(user);
        return UserResponseDto.from(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public SigninResponseDto login(String email, String password) {
        User user = userRepository.findByEmailOrElseThrow(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BaseException(ExceptionCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getUserRole().name());

        return new SigninResponseDto(user.getId(), accessToken, refreshToken, user.getEmail());
    }

    // 회원정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(String email) {
        User user = userRepository.findByEmailOrElseThrow(email);

        return UserResponseDto.from(user);
    }

    // 회원정보 업데이트
    @Transactional
    public UserResponseDto updateUserInfo(
        Long userId,
        String name,
        int age,
        String address
    ) {
        User user = userRepository.findByIdOrElseThrow(userId);

        user.updateInfo(name, age, address);
        return UserResponseDto.from(user);
    }

    // 핸드폰 번호 업데이트
    @Transactional
    public UserResponseDto updatePhone(String email, String newPhone) {
        User user = userRepository.findByEmailOrElseThrow(email);

        user.updatePhone(newPhone);
        return UserResponseDto.from(user);
    }

    // 비밀번호 업데이트
    @Transactional
    public void updatePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmailOrElseThrow(email);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BaseException(ExceptionCode.INVALID_OLD_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    // 토큰 재발급
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

    // 로그아웃
    @Transactional
    public void logout(String email, String bearerToken) {
        String token = jwtTokenProvider.substringToken(bearerToken);
        long remaining = jwtTokenProvider.getRemainingTime(token);
        jwtTokenProvider.blacklistAccessToken(token, remaining);
        redisService.deleteRefreshToken(email);
    }
}