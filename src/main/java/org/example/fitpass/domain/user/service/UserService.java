package org.example.fitpass.domain.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.s3.service.S3Service;
import org.example.fitpass.common.config.RedisService;
import org.example.fitpass.domain.user.dto.response.SigninResponseDto;
import org.example.fitpass.domain.user.dto.response.UserResponseDto;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUser(String email) {
        User user = userRepository.findByEmailOrElseThrow(email);

        if (user.getUserRole() != UserRole.ADMIN) {
            throw new BaseException(ExceptionCode.INVALID_REJECTION_REQUEST);
        }

        return userRepository.findAll().stream()
            .map(UserResponseDto::from)
            .toList();
    }

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
        log.info("[USER SIGNUP] 회원가입 시도 - EMAIL: {}, NAME: {}, PHONE: {}, AGE: {}, GENDER: {}", 
                email, name, phone, age, gender);
        
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
        
        log.info("[USER SIGNUP SUCCESS] 회원가입 완료 - USER_ID: {}, EMAIL: {}, NAME: {}", 
                user.getId(), email, name);
        
        return UserResponseDto.from(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public SigninResponseDto login(String email, String password) {
        log.info("[USER LOGIN] 로그인 시도 - EMAIL: {}", email);
        
        User user = userRepository.findByEmailOrElseThrow(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("[USER LOGIN FAILED] 비밀번호 불일치 - EMAIL: {}, USER_ID: {}", email, user.getId());
            throw new BaseException(ExceptionCode.PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getUserRole().name());

        log.info("[USER LOGIN SUCCESS] 로그인 완료 - USER_ID: {}, EMAIL: {}, ROLE: {}", 
                user.getId(), email, user.getUserRole().name());

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
        String address,
        String phone,
        String img,
        String password
    ) {
        User user = userRepository.findByIdOrElseThrow(userId);

        String encodedPassword = null;

        if (password != null && !password.isBlank()) {
            encodedPassword = passwordEncoder.encode(password);
        }

        user.updateInfo(name, age, address, phone, img, encodedPassword);
        return UserResponseDto.from(user);
    }

    // 토큰 재발급
    @Transactional
    public SigninResponseDto reissueToken(String refreshToken) {
        log.info("[TOKEN REISSUE] 토큰 재발급 시도");
        
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("[TOKEN REISSUE FAILED] 유효하지 않은 토큰");
            throw new BaseException(ExceptionCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getUserEmail(jwtTokenProvider.substringToken(refreshToken));

        // Redis에서 refreshToken이 유효한지 확인
        String storedToken = redisService.getRefreshToken(email);
        if (!refreshToken.equals(storedToken)) {
            log.warn("[TOKEN REISSUE FAILED] Redis 토큰 불일치 - EMAIL: {}", email);
            throw new BaseException(ExceptionCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByEmailOrElseThrow(email);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserRole().name());

        log.info("[TOKEN REISSUE SUCCESS] 토큰 재발급 완료 - USER_ID: {}, EMAIL: {}", 
                user.getId(), email);

        return new SigninResponseDto(user.getId(), newAccessToken, refreshToken, email);
    }

    // 로그아웃
    @Transactional
    public void logout(String email, String bearerToken) {
        log.info("[USER LOGOUT] 로그아웃 시도 - EMAIL: {}", email);
        
        String token = jwtTokenProvider.substringToken(bearerToken);
        long remaining = jwtTokenProvider.getRemainingTime(token);
        jwtTokenProvider.blacklistAccessToken(token, remaining);
        redisService.deleteRefreshToken(email);
        
        log.info("[USER LOGOUT SUCCESS] 로그아웃 완료 - EMAIL: {}", email);
    }

    // Owner로 전환 승인 요청
    @Transactional
    public UserResponseDto requestOwnerUpgrade(String email) {
        log.info("[OWNER UPGRADE REQUEST] 사업자 전환 요청 - EMAIL: {}", email);
        
        User user = userRepository.findByEmailOrElseThrow(email);

        if (user.getUserRole() != UserRole.USER) {
            log.warn("[OWNER UPGRADE REQUEST FAILED] 이미 사업자이거나 대기중 - EMAIL: {}, CURRENT_ROLE: {}", 
                    email, user.getUserRole());
            throw new BaseException(ExceptionCode.INVALID_UPGRADE_REQUEST);
        }

        user.requestOwnerUpgrade();
        
        log.info("[OWNER UPGRADE REQUEST SUCCESS] 사 업자 전환 요청 완료 - USER_ID: {}, EMAIL: {}",
                user.getId(), email);
        
        return UserResponseDto.from(user);
    }

    // Admin용 승인 메서드
    @Transactional
    public UserResponseDto approveOwnerUpgrade(Long userId) {
        log.info("[ADMIN OWNER APPROVE] 사업자 승인 시도 - TARGET_USER_ID: {}", userId);
        
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.getUserRole() != UserRole.PENDING_OWNER) {
            log.warn("[ADMIN OWNER APPROVE FAILED] 승인 대기 상태가 아님 - USER_ID: {}, CURRENT_ROLE: {}", 
                    userId, user.getUserRole());
            throw new BaseException(ExceptionCode.INVALID_APPROVAL_REQUEST);
        }

        user.approveOwnerUpgrade();
        
        log.info("[ADMIN OWNER APPROVE SUCCESS] 사업자 승인 완료 - USER_ID: {}, EMAIL: {}", 
                userId, user.getEmail());
        
        return UserResponseDto.from(user);
    }
    // Admin용 거절 메서드
    @Transactional
    public UserResponseDto rejectOwnerUpgrade(Long userId) {
        log.info("[ADMIN OWNER REJECT] 사업자 거절 시도 - TARGET_USER_ID: {}", userId);
        
        User user = userRepository.findByIdOrElseThrow(userId);

        if (user.getUserRole() != UserRole.PENDING_OWNER) {
            log.warn("[ADMIN OWNER REJECT FAILED] 승인 대기 상태가 아님 - USER_ID: {}, CURRENT_ROLE: {}", 
                    userId, user.getUserRole());
            throw new BaseException(ExceptionCode.INVALID_REJECTION_REQUEST);
        }

        user.rejectOwnerUpgrade();
        
        log.info("[ADMIN OWNER REJECT SUCCESS] 사업자 거절 완료 - USER_ID: {}, EMAIL: {}", 
                userId, user.getEmail());
        
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
    @Transactional(readOnly = true)
    public void checkPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(encodedPassword, rawPassword)) {
            throw new BaseException(ExceptionCode.PASSWORD_NOT_MATCH);
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        return UserResponseDto.from(user);
    }

}