package org.example.fitpass.membership.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.membership.dto.request.MembershipRequestDto;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("MembershipController 통합 테스트")
class MembershipControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private MembershipRepository membershipRepository;

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

    private User savedOwner;
    private User savedUser;
    private Gym savedGym;
    private Membership savedMembership;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        membershipRepository.deleteAll();
        gymRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 데이터 설정
        savedOwner = userRepository.save(new User(
            "owner@test.com",
            null,
            "password123",
            "체육관사장",
            "010-1234-5678",
            35,
            "서울시 강남구",
            Gender.MAN,
            UserRole.OWNER,
            "LOCAL"
        ));

        savedUser = userRepository.save(new User(
            "user@test.com",
            null,
            "password123",
            "일반사용자",
            "010-9876-5432",
            25,
            "서울시 서초구",
            Gender.WOMAN,
            UserRole.USER,
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
    }

    @Test
    @DisplayName("이용권 등록 API 통합 테스트 - 성공")
    void createMembership_Integration_Success() throws Exception {
        // given
        MembershipRequestDto requestDto = new MembershipRequestDto(
            "3개월 자유 이용권", 200000, "3개월 헬스장 자유 이용", 90
        );

        CustomUserDetails ownerDetails = new CustomUserDetails(savedOwner);

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships", savedGym.getId())
                .with(user(ownerDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("3개월 자유 이용권"))
            .andExpect(jsonPath("$.data.price").value(200000))
            .andExpect(jsonPath("$.data.durationInDays").value(90));
    }

    @Test
    @DisplayName("체육관 이용권 조회 API 통합 테스트")
    void getAllMemberships_Integration_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/gyms/{gymId}/memberships", savedGym.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].name").value("1개월 자유 이용권"))
            .andExpect(jsonPath("$.data[0].price").value(80000));
    }

    @Test
    @DisplayName("이용권 상세 조회 API 통합 테스트")
    void getMembershipById_Integration_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/gyms/{gymId}/memberships/{membershipId}", 
                savedGym.getId(), savedMembership.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(savedMembership.getId()))
            .andExpect(jsonPath("$.data.name").value("1개월 자유 이용권"))
            .andExpect(jsonPath("$.data.content").value("헬스장 자유 이용 가능"));
    }

    @Test
    @DisplayName("이용권 정보 수정 API 통합 테스트")
    void updateMembership_Integration_Success() throws Exception {
        // given
        MembershipRequestDto updateDto = new MembershipRequestDto(
            "2개월 자유 이용권", 150000, "2개월 헬스장 자유 이용", 60
        );

        CustomUserDetails ownerDetails = new CustomUserDetails(savedOwner);

        // when & then
        mockMvc.perform(patch("/gyms/{gymId}/memberships/{membershipId}",
                savedGym.getId(), savedMembership.getId())
                .with(user(ownerDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("2개월 자유 이용권"))
            .andExpect(jsonPath("$.data.price").value(150000))
            .andExpect(jsonPath("$.data.durationInDays").value(60));
    }

    @Test
    @DisplayName("이용권 삭제 API 통합 테스트")
    void deleteMembership_Integration_Success() throws Exception {
        CustomUserDetails ownerDetails = new CustomUserDetails(savedOwner);

        // when & then
        mockMvc.perform(delete("/gyms/{gymId}/memberships/{membershipId}", 
                savedGym.getId(), savedMembership.getId())
                .with(user(ownerDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 체육관으로 이용권 등록 시 404 에러")
    void createMembership_GymNotFound_Integration() throws Exception {
        // given
        MembershipRequestDto requestDto = new MembershipRequestDto(
            "테스트 이용권", 50000, "테스트용 이용권입니다. 충분한 글자수를 맞추기 위한 설명입니다", 15
        );
        CustomUserDetails ownerDetails = new CustomUserDetails(savedOwner);

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/memberships", 999L)
                .with(user(ownerDetails)) // 인증 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("존재하지 않는 이용권 조회 시 404 에러")
    void getMembershipById_NotFound_Integration() throws Exception {
        // when & then
        mockMvc.perform(get("/gyms/{gymId}/memberships/{membershipId}", 
                savedGym.getId(), 999L)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("빈 이용권 목록 조회")
    void getAllMemberships_EmptyList_Integration() throws Exception {
        // given - 이용권이 없는 새로운 체육관 생성
        User newOwner = userRepository.save(new User(
            "newowner@test.com", null, "password123", "새사장",
            "010-7777-7777", 30, "서울시 송파구",
            Gender.WOMAN, UserRole.OWNER, "LOCAL"
        ));

        Gym newGym = gymRepository.save(Gym.of(
            List.of("new1.jpg"),
            "새 헬스장",
            "02-7777-7777",
            "새로운 헬스장",
            "서울시",
            "송파구",
            "잠실로 100",
            LocalTime.of(5, 0),
            LocalTime.of(23, 0),
            "새로운 헬스장",
            newOwner
        ));

        // when & then
        mockMvc.perform(get("/gyms/{gymId}/memberships", newGym.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }
}
