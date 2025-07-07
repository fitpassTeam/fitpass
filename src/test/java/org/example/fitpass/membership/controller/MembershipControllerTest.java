package org.example.fitpass.membership.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.membership.controller.MembershipController;
import org.example.fitpass.domain.membership.dto.request.MembershipRequestDto;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.service.MembershipService;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("MembershipController 단위 테스트")
class MembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MembershipService membershipService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> customStringRedisTemplate;

    @MockBean
    @Qualifier("notifyRedisTemplate")
    private RedisTemplate<String, List<Notify>> notifyRedisTemplate;

    private User mockOwner;
    private MembershipResponseDto membershipResponse;
    private MembershipRequestDto membershipRequest;
    private CustomUserDetails ownerUserDetails;

    @BeforeEach
    void setUp() {
        mockOwner = new User(
            "owner@test.com", null, "password123", "체육관사장",
            "010-1234-5678", 35, "서울시 강남구",
            Gender.MAN, UserRole.OWNER, "LOCAL"
        );

        ReflectionTestUtils.setField(mockOwner, "id", 1L);
        ownerUserDetails = new CustomUserDetails(mockOwner);

        membershipResponse = new MembershipResponseDto(
            1L, "1개월 자유 이용권", 80000, "헬스장 자유 이용 가능", 30
        );

        membershipRequest = new MembershipRequestDto(
            "1개월 자유 이용권", 80000, "헬스장 자유 이용 가능", 30
        );
    }

    @Test
    @DisplayName("이용권 등록 - 성공")
    void createMembership_Success() throws Exception {
        // given
        given(membershipService.createMembership(anyLong(), anyLong(), anyString(), anyInt(), anyString(), anyInt()))
            .willReturn(membershipResponse);

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships", 1L)
                .with(user(ownerUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(membershipRequest)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.name").value("1개월 자유 이용권"))
            .andExpect(jsonPath("$.data.price").value(80000));
    }

    @Test
    @DisplayName("이용권 등록 - 체육관 소유자가 아닌 경우")
    void createMembership_NotGymOwner() throws Exception {
        // given
        given(membershipService.createMembership(anyLong(), anyLong(), anyString(), anyInt(), anyString(), anyInt()))
            .willThrow(new BaseException(ExceptionCode.NOT_GYM_OWNER));

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships", 1L)
                .with(user(ownerUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(membershipRequest)))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("체육관 이용권 조회 - 성공")
    void getAllMemberships_Success() throws Exception {
        // given
        List<MembershipResponseDto> memberships = List.of(membershipResponse);
        given(membershipService.getAllByGym(anyLong()))
            .willReturn(memberships);

        // when & then
        mockMvc.perform(get("/gyms/{gymId}/memberships", 1L))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(1L))
            .andExpect(jsonPath("$.data[0].name").value("1개월 자유 이용권"));
    }

    @Test
    @DisplayName("체육관 이용권 조회 - 존재하지 않는 체육관")
    void getAllMemberships_GymNotFound() throws Exception {
        // given
        given(membershipService.getAllByGym(anyLong()))
            .willThrow(new BaseException(ExceptionCode.GYM_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/gyms/{gymId}/memberships", 999L))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("이용권 상세 조회 - 성공")
    void getMembershipById_Success() throws Exception {
        // given
        given(membershipService.getMembershipById(anyLong(), anyLong()))
            .willReturn(membershipResponse);

        // when & then
        mockMvc.perform(get("/gyms/{gymId}/memberships/{membershipId}", 1L, 1L))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.name").value("1개월 자유 이용권"));
    }

    @Test
    @DisplayName("이용권 상세 조회 - 존재하지 않는 이용권")
    void getMembershipById_MembershipNotFound() throws Exception {
        // given
        given(membershipService.getMembershipById(anyLong(), anyLong()))
            .willThrow(new BaseException(ExceptionCode.MEMBERSHIP_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/gyms/{gymId}/memberships/{membershipId}", 1L, 999L))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("이용권 정보 수정 - 성공")
    void updateMembership_Success() throws Exception {
        // given
        MembershipResponseDto updatedResponse = new MembershipResponseDto(
            1L, "2개월 자유 이용권", 150000, "2개월 헬스장 자유 이용", 60
        );
        given(membershipService.updateMembership(anyLong(), anyLong(), anyLong(), anyString(), anyInt(), anyString(), anyInt()))
            .willReturn(updatedResponse);

        MembershipRequestDto updateRequest = new MembershipRequestDto(
            "2개월 자유 이용권", 150000, "2개월 헬스장 자유 이용", 60
        );

        // when & then
        mockMvc.perform(patch("/gyms/{gymId}/memberships/{membershipId}", 1L, 1L)
                .with(user(ownerUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("2개월 자유 이용권"))
            .andExpect(jsonPath("$.data.price").value(150000));
    }

    @Test
    @DisplayName("이용권 정보 수정 - 권한 없음")
    void updateMembership_NotAuthorized() throws Exception {
        // given
        given(membershipService.updateMembership(anyLong(), anyLong(), anyLong(), anyString(), anyInt(), anyString(), anyInt()))
            .willThrow(new BaseException(ExceptionCode.NOT_GYM_OWNER));

        // when & then
        mockMvc.perform(patch("/gyms/{gymId}/memberships/{membershipId}", 1L, 1L)
                .with(user(ownerUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(membershipRequest)))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("이용권 삭제 - 성공")
    void deleteMembership_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/gyms/{gymId}/memberships/{membershipId}", 1L, 1L)
                .with(user(ownerUserDetails)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이용권 삭제 - 권한 없음")
    void deleteMembership_NotAuthorized() throws Exception {
        // given
        doThrow(new BaseException(ExceptionCode.NOT_GYM_OWNER))
            .when(membershipService).deleteMembership(anyLong(), anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/gyms/{gymId}/memberships/{membershipId}", 1L, 1L)
                .with(user(ownerUserDetails)))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }
}
