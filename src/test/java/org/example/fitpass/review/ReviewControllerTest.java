package org.example.fitpass.review;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.common.config.RedisService;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.review.dto.response.ReviewResponseDto;
import org.example.fitpass.domain.review.service.ReviewService;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> customStringRedisTemplate;

    @MockBean
    @Qualifier("notifyRedisTemplate")
    private RedisTemplate<String, List<Notify>> notifyRedisTemplate;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        user = new User(
            "test@test.com",
            "profile.jpg",
            "1234",
            "테스트유저",
            "010-1234-5678",
            25,
            "서울시 강남구",
            Gender.MAN,
            UserRole.USER
        );
        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createReview_success() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.of(2024, 7, 1, 12, 0);
        ReviewResponseDto responseDto = new ReviewResponseDto(
            1L,
            "좋아요",
            5,
            4,
            "test@test.com",
            "김트레이너",
            "헬스장",
            now
        );

        given(reviewService.createReview(anyLong(), anyLong(), anyString(), anyInt(), anyInt()))
            .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/reservations/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "content": "좋아요",
                    "gymRating": 5,
                    "trainerRating": 4
                }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.content").value("좋아요"))
            .andExpect(jsonPath("$.data.gymRating").value(5))
            .andExpect(jsonPath("$.data.trainerRating").value(4));
    }
}
