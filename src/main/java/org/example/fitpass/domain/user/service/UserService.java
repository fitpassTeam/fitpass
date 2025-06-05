package org.example.fitpass.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.auth.dto.response.SigninResponseDto;
import org.example.fitpass.domain.user.dto.LoginRequestDto;
import org.example.fitpass.domain.user.dto.UserRequestDto;
import org.example.fitpass.domain.user.dto.UserResponseDto;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserResponseDto signup(UserRequestDto dto) {
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

    public SigninResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String bearerToken = jwtTokenProvider.createToken(user.getEmail(), user.getUserRole().name());

        return new SigninResponseDto(user.getId(), bearerToken, user.getEmail());
    }

    public UserResponseDto getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return UserResponseDto.from(user);
    }

    public UserResponseDto updateUserInfo(String email, UserRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.updateInfo(dto); // 엔티티에 해당 로직 필요
        return UserResponseDto.from(user);
    }
}