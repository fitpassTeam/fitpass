package org.example.fitpass.domain.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.dto.response.SigninResponseDto;
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
        Gender gender
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
                UserRole.USER
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

    // Owner로 전환 승인 요청
    @Transactional
    public UserResponseDto requestOwnerUpgrade(String email) {
        User user = userRepository.findByEmailOrElseThrow(email);

        if (user.getUserRole() != UserRole.USER) {
            throw new BaseException(ExceptionCode.INVALID_UPGRADE_REQUEST);
        }

        user.requestOwnerUpgrade();
        return UserResponseDto.from(user);
    }

    // Admin용 승인 메서드
    @Transactional
    public UserResponseDto approveOwnerUpgrade(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.getUserRole() != UserRole.PENDING_OWNER) {
            throw new BaseException(ExceptionCode.INVALID_APPROVAL_REQUEST);
        }

        user.approveOwnerUpgrade();
        return UserResponseDto.from(user);
    }
    // Admin용 거절 메서드
    @Transactional
    public UserResponseDto rejectOwnerUpgrade(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.getUserRole() != UserRole.PENDING_OWNER) {
            throw new BaseException(ExceptionCode.INVALID_REJECTION_REQUEST);
        }

        user.rejectOwnerUpgrade();
        return UserResponseDto.from(user);
    }

    // 승인 대기 목록 조회
    @Transactional(readOnly = true)
    public List<UserResponseDto> getPendingOwnerRequests() {
        List<User> pendingUsers = userRepository.findByUserRole(UserRole.PENDING_OWNER);
        return pendingUsers.stream()
            .map(UserResponseDto::from)
            .toList();
    }
}