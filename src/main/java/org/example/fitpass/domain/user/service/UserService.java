package org.example.fitpass.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponseDto signup(UserRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BaseException(ExceptionCode.USER_ALREADY_EXISTS);
        }

        User user = new User(
                dto.getEmail(),
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
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BaseException(ExceptionCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BaseException(ExceptionCode.INVALID_PASSWORD);
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getUserRole().name());

        return new SigninResponseDto(user.getId(), token, user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ExceptionCode.USER_NOT_FOUND));

        return UserResponseDto.from(user);
    }


    @Transactional
    public UserResponseDto updateUserInfo(String email, UserRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ExceptionCode.USER_NOT_FOUND));

        user.updateInfo(dto);
        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto updatePhone(String email, String newPhone) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ExceptionCode.USER_NOT_FOUND));

        user.updatePhone(newPhone);
        return UserResponseDto.from(user);
    }

    @Transactional
    public void updatePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ExceptionCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BaseException(ExceptionCode.INVALID_OLD_PASSWORD);
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
    }
}