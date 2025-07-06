package org.example.fitpass.membership.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.membership.controller.MembershipPurchaseController;
import org.example.fitpass.domain.membership.dto.response.MembershipPurchaseResponseDto;
import org.example.fitpass.domain.membership.service.MembershipPurchaseService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("MembershipPurchaseController 단위 테스트")
class MembershipPurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @MockBean
    private MembershipPurchaseService membershipPurchaseService;

    private User mockOwner;
    private MembershipPurchaseResponseDto purchaseResponse;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        // 일반 사용자 (이용권 구매자)
        User mockUser = new User(
            "user@test.com", null, "password123", "일반사용자",
            "010-1234-5678", 25, "서울시 강남구",
            Gender.MAN, UserRole.USER, "LOCAL"
        );

        ReflectionTestUtils.setField(mockUser, "id", 1L);
        userDetails = new CustomUserDetails(mockUser); // 변수명도 userDetails로 변경

        purchaseResponse = new MembershipPurchaseResponseDto(
            1L, "1개월 자유 이용권", 80000, 30,
            LocalDateTime.now(), LocalDateTime.now().plusDays(30), true
        );
    }

    @Test
    @DisplayName("이용권 구매 - 성공")
    void purchase_Success() throws Exception {
        // given
        given(membershipPurchaseService.purchase(anyLong(), anyLong(), anyLong(), any()))
            .willReturn(purchaseResponse);

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships/{membershipId}/purchase", 1L, 1L)
                .with(user(userDetails)) // 인증 추가
                .param("activationDate", "2025-07-10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.membershipName").value("1개월 자유 이용권"))
            .andExpect(jsonPath("$.data.price").value(80000));
    }

    @Test
    @DisplayName("이용권 구매 - 존재하지 않는 이용권")
    void purchase_MembershipNotFound() throws Exception {
        // given
        given(membershipPurchaseService.purchase(anyLong(), anyLong(), anyLong(), any()))
            .willThrow(new BaseException(ExceptionCode.MEMBERSHIP_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships/{membershipId}/purchase", 1L, 999L)
                .with(user(userDetails))
                .param("activationDate", "2025-07-10"))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("이용권 구매 - 잘못된 활성화 날짜 (과거)")
    void purchase_InvalidActivationDatePast() throws Exception {
        // given
        given(membershipPurchaseService.purchase(anyLong(), anyLong(), anyLong(), any()))
            .willThrow(new BaseException(ExceptionCode.INVALID_ACTIVATION_DATE_PAST));

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships/{membershipId}/purchase", 1L, 1L)
                .with(user(userDetails))
                .param("activationDate", "2025-01-01"))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("이용권 구매 - 잘못된 활성화 날짜 (너무 먼 미래)")
    void purchase_InvalidActivationDateTooFar() throws Exception {
        // given
        given(membershipPurchaseService.purchase(anyLong(), anyLong(), anyLong(), any()))
            .willThrow(new BaseException(ExceptionCode.INVALID_ACTIVATION_DATE_TOO_FAR));

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships/{membershipId}/purchase", 1L, 1L)
                .with(user(userDetails))
                .param("activationDate", "2025-12-31"))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("사용 가능한 이용권 조회 - 성공")
    void getNotStartedMemberships_Success() throws Exception {
        // given
        List<MembershipPurchaseResponseDto> notStartedMemberships = List.of(purchaseResponse);
        given(membershipPurchaseService.getNotStartedMemberships(anyLong()))
            .willReturn(notStartedMemberships);

        // when & then
        mockMvc.perform(get("/memberships/purchases/not-started")
                .with(user(userDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    @DisplayName("사용 가능한 이용권 조회 - 빈 목록")
    void getNotStartedMemberships_EmptyList() throws Exception {
        // given
        given(membershipPurchaseService.getNotStartedMemberships(anyLong()))
            .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/memberships/purchases/not-started")
                .with(user(userDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("구매 이력 조회 - 성공")
    void getMyPurchases_Success() throws Exception {
        // given
        List<MembershipPurchaseResponseDto> purchases = List.of(purchaseResponse);
        given(membershipPurchaseService.getMyPurchases(anyLong()))
            .willReturn(purchases);

        // when & then
        mockMvc.perform(get("/memberships/purchases/me")
                .with(user(userDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].membershipName").value("1개월 자유 이용권"));
    }

    @Test
    @DisplayName("현재 활성 이용권 조회 - 성공")
    void getMyActiveMembership_Success() throws Exception {
        // given
        given(membershipPurchaseService.getMyActive(anyLong()))
            .willReturn(purchaseResponse);

        // when & then
        mockMvc.perform(get("/memberships/purchases/active")
                .with(user(userDetails)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("현재 활성 이용권 조회 - 활성 이용권 없음")
    void getMyActiveMembership_NotActive() throws Exception {
        // given
        given(membershipPurchaseService.getMyActive(anyLong()))
            .willThrow(new BaseException(ExceptionCode.MEMBERSHIP_NOT_ACTIVE));

        // when & then
        mockMvc.perform(get("/memberships/purchases/active")
                .with(user(userDetails)))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }
}
