package org.example.fitpass.fitnessGoal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.List;

import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.fitnessGoal.dto.request.DailyRecordCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.request.FitnessGoalCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.request.FitnessGoalUpdateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.request.WeightRecordCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FitnessGoalScenarioTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private User user;
    private Long fitnessGoalId;
    private Long weightRecordId;
    private Long dailyRecordId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(
            "fitgoaluser@test.com",
            "profile.jpg",
            "password",
            "피트니스유저",
            "010-0000-0000",
            25,
            "서울시 강남구",
            Gender.MAN,
            UserRole.USER));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @Test
    void fitnessGoalFullScenario() throws Exception {
        // 1. 목표 생성
        FitnessGoalCreateRequestDto createDto = new FitnessGoalCreateRequestDto(
            "체중 감량 목표",
            "3개월 동안 5kg 감량",
            GoalType.WEIGHT_LOSS, // enum 이름 문자열로 맞춰야 함
            70.0,
            65.0,
            LocalDate.now(),
            LocalDate.now().plusMonths(3));

        String createResponse = mockMvc.perform(post("/fitness-goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.title").value("체중 감량 목표"))
            .andReturn().getResponse().getContentAsString();

        Number idNum = JsonPath.read(createResponse, "$.data.id");
        fitnessGoalId = idNum.longValue();

        // 2. 체중 기록 추가
        WeightRecordCreateRequestDto weightCreateDto = new WeightRecordCreateRequestDto(
            69.0,
            LocalDate.now(),
            "첫 체중 기록");

        String weightCreateResponse = mockMvc.perform(post("/fitness-goals/" + fitnessGoalId + "/weight-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weightCreateDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.weight").value(69.0))
            .andReturn().getResponse().getContentAsString();

        Number weightRecordIdNum = JsonPath.read(weightCreateResponse, "$.data.id");
        weightRecordId = weightRecordIdNum.longValue();

        // 3. 일일 기록 추가
        DailyRecordCreateRequestDto dailyRecordCreateDto = new DailyRecordCreateRequestDto(
            List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"),
            "운동 기록 메모",
            LocalDate.now());

        String dailyRecordResponse = mockMvc.perform(post("/fitness-goals/" + fitnessGoalId + "/daily-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dailyRecordCreateDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.memo").value("운동 기록 메모"))
            .andReturn().getResponse().getContentAsString();

        Number dailyRecordIdNum = JsonPath.read(dailyRecordResponse, "$.data.id");
        dailyRecordId = dailyRecordIdNum.longValue();

        // 4. 체중 기록 수정
        WeightRecordCreateRequestDto weightUpdateDto = new WeightRecordCreateRequestDto(
            68.5,
            LocalDate.now(),
            "수정된 체중 기록 메모");

        mockMvc.perform(put("/fitness-goals/" + fitnessGoalId + "/weight-records/" + weightRecordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weightUpdateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.weight").value(68.5));

        // 5. 목표 수정
        FitnessGoalUpdateRequestDto updateGoalDto = new FitnessGoalUpdateRequestDto(
            "체중 감량 목표 - 수정",
            "4개월 동안 6kg 감량 목표로 변경",
            64.0,
            LocalDate.now().plusMonths(4));

        mockMvc.perform(put("/fitness-goals/" + fitnessGoalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateGoalDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("체중 감량 목표 - 수정"))
            .andExpect(jsonPath("$.data.targetWeight").value(64.0));

        // 6. 일일 기록 삭제
        mockMvc.perform(delete("/fitness-goals/" + fitnessGoalId + "/daily-records/" + dailyRecordId))
            .andExpect(status().isNoContent());

        // 7. 체중 기록 삭제
        mockMvc.perform(delete("/fitness-goals/" + fitnessGoalId + "/weight-records/" + weightRecordId))
            .andExpect(status().isNoContent());

        // 8. 목표 삭제
        mockMvc.perform(delete("/fitness-goals/" + fitnessGoalId))
            .andExpect(status().isNoContent());
    }
}
