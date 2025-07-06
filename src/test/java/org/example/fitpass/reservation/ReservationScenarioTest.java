//package org.example.fitpass.reservation;
//
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.jayway.jsonpath.JsonPath;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.Collections;
//import org.example.fitpass.common.jwt.JwtTokenProvider;
//import org.example.fitpass.common.security.CustomUserDetails;
//import org.example.fitpass.config.RedisService;
//import org.example.fitpass.domain.gym.entity.Gym;
//import org.example.fitpass.domain.gym.repository.GymRepository;
//import org.example.fitpass.domain.point.service.PointService;
//import org.example.fitpass.domain.reservation.repository.ReservationRepository;
//import org.example.fitpass.domain.trainer.entity.Trainer;
//import org.example.fitpass.domain.trainer.repository.TrainerRepository;
//import org.example.fitpass.domain.user.entity.User;
//import org.example.fitpass.domain.user.enums.Gender;
//import org.example.fitpass.domain.user.enums.UserRole;
//import org.example.fitpass.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@ActiveProfiles("test")
//class ReservationScenarioTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private TrainerRepository trainerRepository;
//
//    @Autowired
//    private GymRepository gymRepository;
//
//    @Autowired
//    private ReservationRepository reservationRepository;
//
//    @Autowired
//    private PointService pointService;
//
//    @MockBean
//    private RedisService redisService;
//
//    @MockBean
//    private JwtTokenProvider jwtTokenProvider;
//
//    @MockBean
//    @Qualifier("customStringRedisTemplate")
//    private RedisTemplate<String, String> customStringRedisTemplate;
//
//    private User user;
//    private Gym gym;
//    private Trainer trainer;
//
//    @BeforeEach
//    void setUp() {
//        user = userRepository.save(new User(
//            "test@test.com", "profile.jpg", "1234", "테스트유저", "010-1234-5678", 25,
//            "서울시 강남구", Gender.MAN, UserRole.USER
//        ));
//
//        // 포인트 충전 (예: 100,000원)
//        pointService.chargePoint(user.getId(), 100000, "테스트 포인트 충전"); // 사용자 엔티티에 직접 포인트 충전
//        userRepository.save(user);
//
//        CustomUserDetails userDetails = new CustomUserDetails(user);
//        SecurityContextHolder.getContext().setAuthentication(
//            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
//        );
//
//        gym = gymRepository.save(new Gym(
//            Collections.emptyList(), "헬스장", "123-45-67890", "좋은 헬스장입니다",
//            "서울", "강남구", "테헤란로 123",
//            LocalTime.of(6, 0), LocalTime.of(23, 0),
//            "최고의 헬스장", user
//        ));
//
//        trainer = new Trainer(Collections.emptyList(), "김트레이너", 50000, "전문 트레이너입니다", "5년 경력");
//        trainer.assignToGym(gym);
//        trainerRepository.save(trainer);
//    }
//
//
//    @Test
//    void 예약생성_조회_수정_취소_시나리오() throws Exception {
//        // 1. 예약 생성
//        String createJson = """
//            {
//              "reservationDate": "%s",
//              "reservationTime": "10:00"
//            }
//        """.formatted(LocalDate.now().plusDays(3)); // 최소 2일 이후
//
//        String createResponse = mockMvc.perform(post("/gyms/" + gym.getId() + "/trainers/" + trainer.getId() + "/reservations")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(createJson))
//            .andExpect(status().isCreated())
//            .andExpect(jsonPath("$.data").exists())
//            .andReturn()
//            .getResponse()
//            .getContentAsString();
//
//        Number reservationIdNumber = JsonPath.read(createResponse, "$.data.reservationId");
//        Long reservationId = reservationIdNumber.longValue();
//
//        // 2. 예약 단건 조회
//        mockMvc.perform(get("/reservations/" + reservationId))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.data.reservationTime").value("10:00"));
//
//        // 3. 예약 수정
//        String updateJson = """
//            {
//              "reservationDate": "%s",
//              "reservationTime": "11:00",
//              "reservationStatus": "PENDING"
//            }
//        """.formatted(LocalDate.now().plusDays(4));
//
//        mockMvc.perform(patch("/gyms/" + gym.getId() + "/trainers/" + trainer.getId() + "/reservations/" + reservationId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(updateJson))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.data.reservationTime").value("11:00"));
//
//        // 4. 예약 취소
//        mockMvc.perform(delete("/gyms/" + gym.getId() + "/trainers/" + trainer.getId() + "/reservations/" + reservationId))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.message").value("예약 취소가 완료되었습니다. 포인트가 환불되었습니다."))
//            .andReturn();
//    }
//}
//
