package org.example.fitpass.membership.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;
import org.example.fitpass.domain.membership.repository.MembershipPurchaseRepository;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;
import org.example.fitpass.domain.point.repository.PointRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("MembershipPurchaseController 통합 테스트")
class MembershipPurchaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MembershipPurchaseRepository purchaseRepository;

    @Autowired
    private PointRepository pointRepository;

    private User savedUser;
    private User savedOwner;
    private Gym savedGym;
    private Membership savedMembership;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        purchaseRepository.deleteAll();
        membershipRepository.deleteAll();
        pointRepository.deleteAll();
        gymRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 데이터 설정
        savedUser = userRepository.save(new User(
            "user@test.com",
            null,
            "password123",
            "일반사용자",
            "010-1234-5678",
            25,
            "서울시 강남구",
            Gender.MAN,
            UserRole.USER,
            "LOCAL"
        ));

        savedOwner = userRepository.save(new User(
            "owner@test.com",
            null,
            "password123",
            "체육관사장",
            "010-9876-5432",
            35,
            "서울시 서초구",
            Gender.WOMAN,
            UserRole.OWNER,
            "LOCAL"
        ));

        savedGym = gymRepository.save(Gym.of(
            List.of("gym1.jpg", "gym2.jpg"),
            "테스트 헬스장",
            "02-1234-5678",
            "최고의 헬스장입니다",
            "서울시",
            "강남구",
            "테헤란로 332",
            LocalTime.of(6, 0),
            LocalTime.of(23, 59),
            "깨끗하고 시설이 좋은 헬스장",
            savedOwner
        ));

        savedMembership = membershipRepository.save(Membership.of(
            "1개월 자유 이용권",
            80000,
            "헬스장 자유 이용 가능",
            30
        ));
        savedMembership.assignToGym(savedGym);

        // 사용자에게 충분한 포인트 지급 및 잔액 업데이트
        pointRepository.save(new Point(savedUser, 100000, "테스트 포인트 지급", 100000, PointType.CHARGE));
        savedUser.updatePointBalance(100000); // 사용자 잔액 업데이트
        savedUser = userRepository.save(savedUser); // 업데이트된 사용자 저장
    }

    @Test
    @DisplayName("이용권 구매 API 통합 테스트 - 성공")
    void purchase_Integration_Success() throws Exception {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        
        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships/{membershipId}/purchase", 
                savedGym.getId(), savedMembership.getId())
                .with(user(userDetails)) // 인증 추가
                .param("activationDate", "2025-07-10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.membershipName").value("1개월 자유 이용권"))
            .andExpect(jsonPath("$.data.price").value(80000))
            .andExpect(jsonPath("$.data.durationInDays").value(30));
    }

    @Test
    @DisplayName("사용 가능한 이용권 조회 API 통합 테스트")
    void getNotStartedMemberships_Integration_Success() throws Exception {
        // given - 구매한 이용권 생성 (미활성화 상태)
        MembershipPurchase purchase = new MembershipPurchase(
            savedMembership, savedGym, savedUser, LocalDateTime.now(), LocalDateTime.now().plusDays(1)
        );
        purchaseRepository.save(purchase);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);

        // when & then
        mockMvc.perform(get("/memberships/purchases/not-started")
                .with(user(userDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].membershipName").value("1개월 자유 이용권"));
    }

    @Test
    @DisplayName("구매 이력 조회 API 통합 테스트")
    void getMyPurchases_Integration_Success() throws Exception {
        // given - 구매 이력 생성 (과거 시간으로 활성화)
        MembershipPurchase purchase1 = new MembershipPurchase(
            savedMembership, savedGym, savedUser, LocalDateTime.now()
        );
        LocalDateTime activationTime = LocalDateTime.now().minusMinutes(10); // 10분 전에 활성화
        purchase1.activate(activationTime);
        purchaseRepository.save(purchase1);

        MembershipPurchase purchase2 = new MembershipPurchase(
            savedMembership, savedGym, savedUser, LocalDateTime.now().minusDays(30)
        );
        purchaseRepository.save(purchase2);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);

        // when & then
        mockMvc.perform(get("/memberships/purchases/me")
                .with(user(userDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("현재 활성 이용권 조회 API 통합 테스트")
    void getMyActiveMembership_Integration_Success() throws Exception {
        // given - 활성 이용권 생성 (과거 시간으로 활성화)
        MembershipPurchase activePurchase = new MembershipPurchase(
            savedMembership, savedGym, savedUser, LocalDateTime.now()
        );
        LocalDateTime activationTime = LocalDateTime.now().minusMinutes(10); // 10분 전에 활성화
        activePurchase.activate(activationTime);
        purchaseRepository.save(activePurchase);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);

        // when & then
        mockMvc.perform(get("/memberships/purchases/active")
                .with(user(userDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.membershipName").value("1개월 자유 이용권"))
            .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("활성 이용권이 없을 때 404 에러")
    void getMyActiveMembership_NotActive_Integration() throws Exception {
        CustomUserDetails ownerDetails = new CustomUserDetails(savedOwner);
        // when & then (활성 이용권 없이 조회)
        mockMvc.perform(get("/memberships/purchases/active")
                .with(user(ownerDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 이용권 구매 시 404 에러")
    void purchase_MembershipNotFound_Integration() throws Exception {
        CustomUserDetails ownerDetails = new CustomUserDetails(savedOwner);
        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships/{membershipId}/purchase", 
                savedGym.getId(), 999L)
                .with(user(ownerDetails)) // 인증 추가
                .param("activationDate", "2025-07-10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("빈 구매 이력 조회")
    void getMyPurchases_EmptyList_Integration() throws Exception {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        
        // when & then (구매 이력 없이 조회)
        mockMvc.perform(get("/memberships/purchases/me")
                .with(user(userDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("빈 미활성화 이용권 목록 조회")
    void getNotStartedMemberships_EmptyList_Integration() throws Exception {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        
        // when & then (미활성화 이용권 없이 조회)
        mockMvc.perform(get("/memberships/purchases/not-started")
                .with(user(userDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }
}
