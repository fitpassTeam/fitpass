package org.example.fitpass.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fitpass.domain.user.dto.request.LoginRequestDto;
import org.example.fitpass.domain.user.dto.request.LogoutRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePasswordRequestDto;
import org.example.fitpass.domain.user.dto.request.UpdatePhoneRequestDto;
import org.example.fitpass.domain.user.dto.request.UserRequestDto;
import org.example.fitpass.domain.user.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserScenarioTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;

    @Test
    @DisplayName("회원가입 → 로그인 → 내 정보 조회 → 전화번호 수정 → 비밀번호 수정 → 로그아웃 시나리오")
    void fullUserFlow() throws Exception {
        // 1. 회원가입
        UserRequestDto signupDto = new UserRequestDto(
            "testuser@example.com",
            "", // userImage
            "Password123!", // 유효한 비밀번호
            "테스트 유저",
            "010-1234-5678",
            28,
            "서울시 마포구",
            Gender.MAN
        );

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.email").value(signupDto.email()));

        // 2. 로그인
        LoginRequestDto loginDto = new LoginRequestDto(signupDto.email(), signupDto.password());
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        accessToken = jsonNode.get("data").get("accessToken").asText();

        // 3. 내 정보 조회
        mockMvc.perform(get("/users/me")
                .header("Authorization", accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value(signupDto.name()));

        // 4. 전화번호 수정
        UpdatePhoneRequestDto phoneDto = new UpdatePhoneRequestDto("010-9999-8888");

        mockMvc.perform(patch("/users/me/phone")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phoneDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.phone").value("010-9999-8888"));

        // 5. 비밀번호 변경
        UpdatePasswordRequestDto pwDto = new UpdatePasswordRequestDto("Password123!", "Newpass456@");

        mockMvc.perform(patch("/users/me/password")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pwDto)))
            .andExpect(status().isOk());

        // 6. 로그아웃
        LogoutRequestDto logoutDto = new LogoutRequestDto(signupDto.email());

        mockMvc.perform(post("/auth/logout")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutDto)))
            .andExpect(status().isOk());
    }
}