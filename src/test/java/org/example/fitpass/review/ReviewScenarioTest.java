package org.example.fitpass.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.UUID;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.repository.ReservationRepository;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ReviewScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private GymRepository gymRepository;

    private User user;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAllInBatch();
        trainerRepository.deleteAllInBatch();
        gymRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

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
        user = userRepository.save(user); // 변경된 부분

        userRepository.flush();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void reviewCreateUpdateReadDeleteScenario() throws Exception {
        // GIVEN: 사용자, 체육관, 트레이너, 예약 데이터 준비
        Gym gym = new Gym(
            Collections.emptyList(), // 이미지 리스트
            "헬스장",
            UUID.randomUUID().toString(), // 고유한 번호 생성
            "좋은 헬스장입니다",
            "서울",
            "강남구",
            "테헤란로 123",
            LocalTime.of(6, 0),
            LocalTime.of(23, 0),
            "최고의 헬스장",
            user
        );
        gym = gymRepository.save(gym); // 변경된 부분
        reservationRepository.flush();

        Trainer trainer = new Trainer(
            Collections.emptyList(), // 이미지 리스트
            "김트레이너",
            50000,
            "전문 트레이너입니다",
            "5년 경력"
        );
        trainer.assignToGym(gym);          // Trainer → Gym
        gym.getTrainers().add(trainer);    // Gym → Trainer 리스트에 추가

        trainerRepository.save(trainer);
        trainerRepository.flush();

        Reservation reservation = new Reservation(
            LocalDate.now().minusDays(1),
            LocalTime.of(10, 0),
            ReservationStatus.COMPLETED,
            user,
            gym,
            trainer
        );
        reservationRepository.save(reservation);
        reservationRepository.flush();
        Long reservationId = reservation.getId();

        // 1. 리뷰 작성
        String content = """
            {
                "content": "좋아요",
                "gymRating": 5,
                "trainerRating": 4
            }
        """;

        String responseBody = mockMvc.perform(post("/reservations/" + reservationId + "/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.content").value("좋아요"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Number reviewIdNum = JsonPath.read(responseBody, "$.data.reviewId");
        Long reviewId = reviewIdNum.longValue();

        // 2. 리뷰 수정
        String updatedContent = """
            {
                "content": "정말 좋아요",
                "gymRating": 4,
                "trainerRating": 5
            }
        """;

        mockMvc.perform(put("/reservations/" + reservationId + "/reviews/" + reviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedContent))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("정말 좋아요"));

        // 3. 리뷰 상세 조회
        mockMvc.perform(get("/reviews/" + reviewId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("정말 좋아요"))
            .andExpect(jsonPath("$.data.trainerRating").value(5));

        // 4. 리뷰 삭제
        mockMvc.perform(delete("/reservations/" + reservationId + "/reviews/" + reviewId))
            .andExpect(status().isNoContent());

        // 5. 삭제된 리뷰 조회 실패 확인
        mockMvc.perform(get("/reviews/" + reviewId))
            .andExpect(status().isNotFound());
    }
}
