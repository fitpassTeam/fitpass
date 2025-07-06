package org.example.fitpass.point;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.point.dto.request.PointUseRefundRequestDto;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class PointScenarioTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PointService pointService;
    @Autowired private ObjectMapper objectMapper;

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

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(
            "pointuser@test.com", "profile.jpg", "1234", "포인트유저", "010-0000-0000", 30,
            "서울시 강남구", Gender.MAN, UserRole.USER
        ));

        // 사용자 인증 세팅
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // 포인트 초기 충전
        pointService.chargePoint(user.getId(), 100000, "초기 충전");
    }

    @Test
    void 포인트_충전_사용_환불_현금화_시나리오() throws Exception {
        // 포인트 사용
        PointUseRefundRequestDto useDto = new PointUseRefundRequestDto(50000, "PT 예약 사용");
        mockMvc.perform(post("/users/points/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(useDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.balance").value(50000));

        // 포인트 환불
        PointUseRefundRequestDto refundDto = new PointUseRefundRequestDto(20000, "예약 취소 환불");
        mockMvc.perform(post("/users/points/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.balance").value(70000));

        // 포인트 현금화 (90% 환불)
        String cashOutJson = """
            {
                "amount": 20000,
                "description": "포인트 현금화 테스트"
            }
        """;

        mockMvc.perform(post("/users/points/cashout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashOutJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.requestedAmount").value(20000))
            .andExpect(jsonPath("$.data.cashAmount").value(18000))
            .andExpect(jsonPath("$.data.newBalance").value(50000));
    }
}