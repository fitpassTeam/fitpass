package org.example.fitpass.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.payment.client.TossPaymentClient;
import org.example.fitpass.domain.payment.dto.request.PaymentRequestDto;
import org.example.fitpass.domain.payment.dto.response.PaymentCancelResponseDto;
import org.example.fitpass.domain.payment.entity.Payment;
import org.example.fitpass.domain.payment.enums.PaymentStatus;
import org.example.fitpass.domain.payment.repository.PaymentRepository;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;
import org.example.fitpass.domain.point.repository.PointRepository;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("결제 시나리오 테스트")
public class PaymentScenarioTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private PointService pointService;
    @Autowired private ObjectMapper objectMapper;

    @Mock
    private RestTemplate restTemplate;

    @MockBean
    private TossPaymentClient tossPaymentClient;

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
        // 테스트 사용자 생성
        user = userRepository.save(new User(
            "paymentuser@test.com", "profile.jpg", "password123", "결제테스트유저", 
            "010-1234-5678", 28, "서울시 강남구", Gender.MAN, UserRole.USER
        ));

        // 사용자 인증 세팅
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @Test
    @DisplayName("결제 전체 시나리오: 준비 → 승인 → 포인트 충전 확인")
    void 결제_전체_성공_시나리오() throws Exception {
        // Given: 결제 요청 데이터
        PaymentRequestDto paymentRequest = new PaymentRequestDto(10000, "피트니스 포인트 충전");

        // When 1: 결제 준비
        mockMvc.perform(post("/api/payments/prepare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.amount").value(10000))
                .andExpect(jsonPath("$.data.customerEmail").value("paymentuser@test.com"))
                .andExpect(jsonPath("$.data.customerName").value("결제테스트유저"));

        // Then 1: 결제 엔티티가 PENDING 상태로 저장되었는지 확인
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(payments).hasSize(1);
        Payment payment = payments.get(0);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getAmount()).isEqualTo(10000);
        assertThat(payment.getOrderName()).isEqualTo("피트니스 포인트 충전");

        // When 2: 결제 승인 (토스 API 호출 없이 직접 서비스 호출)
        String paymentKey = "test_payment_key_" + System.currentTimeMillis();
        
        // 실제로는 토스 API를 호출하지만, 테스트에서는 서비스 직접 호출로 대체
        // paymentService.confirmPayment(paymentKey, orderId, 10000);

        // 대신 수동으로 결제 승인 처리
        payment.updatePaymentKey(paymentKey);
        payment.updateStatus(PaymentStatus.CONFIRMED);
        payment.updateMethod("카드");
        
        // 포인트 충전
        pointService.chargePoint(user.getId(), 10000, "토스페이먼츠 충전 - " + payment.getOrderName());

        // Then 2: 결제 상태가 CONFIRMED로 변경되었는지 확인
        Payment confirmedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(confirmedPayment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(confirmedPayment.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(confirmedPayment.getMethod()).isEqualTo("카드");

        // Then 3: 포인트가 정상적으로 충전되었는지 확인
        List<Point> pointHistory = pointRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(pointHistory).hasSize(1);
        Point chargePoint = pointHistory.get(0);
        assertThat(chargePoint.getPointType()).isEqualTo(PointType.CHARGE);
        assertThat(chargePoint.getAmount()).isEqualTo(10000);
        assertThat(chargePoint.getDescription()).contains("토스페이먼츠 충전");

        // When 3: 결제 내역 조회
        mockMvc.perform(get("/api/payments/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].amount").value(10000))
                .andExpect(jsonPath("$.data[0].status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("결제 실패 시나리오")
    void 결제_실패_시나리오() throws Exception {
        // Given: 결제 요청
        PaymentRequestDto paymentRequest = new PaymentRequestDto(5000, "테스트 충전");

        // When 1: 결제 준비
        mockMvc.perform(post("/api/payments/prepare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());

        // Given 2: 생성된 결제 주문 ID 가져오기
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        String orderId = payments.get(0).getOrderId();

        // When 2: 결제 실패 처리
        mockMvc.perform(post("/api/payments/fail")
                .param("orderId", orderId)
                .param("message", "카드 한도 초과"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));

        // Then: 결제 상태가 FAILED로 변경되었는지 확인
        Payment failedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(failedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(failedPayment.getFailureReason()).isEqualTo("카드 한도 초과");

        // Then: 포인트는 충전되지 않았는지 확인
        List<Point> pointHistory = pointRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(pointHistory).isEmpty();
    }

    @Test
    @WithMockUser(username = "paymentuser@test.com", roles = "USER")
    @DisplayName("결제 취소 시나리오")
    void 결제_취소_시나리오() throws Exception {
        // Given: 결제 준비 요청
        PaymentRequestDto paymentRequest = new PaymentRequestDto(20000, "취소 테스트 충전");

        mockMvc.perform(post("/api/payments/prepare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
            .andExpect(status().isOk());

        // 승인 처리 수동 변경
        Payment payment = paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).get(0);
        payment.updatePaymentKey("test_payment_key_for_cancel");
        payment.updateStatus(PaymentStatus.CONFIRMED);
        payment.updateMethod("카드");

        // 포인트 충전
        pointService.chargePoint(user.getId(), 20000, "토스페이먼츠 충전 - " + payment.getOrderName());

        // 토스페이먼츠 취소 응답 DTO 준비 및 mocking
        PaymentCancelResponseDto cancelResponse = new PaymentCancelResponseDto(
            "test_payment_key_for_cancel",
            payment.getOrderId(),
            "취소 테스트 충전",
            20000,
            "CANCELED",
            LocalDateTime.now(), // cancelledAt
            "카드"
        );

        given(tossPaymentClient.cancelPayment("test_payment_key_for_cancel", "고객 변심"))
            .willReturn(cancelResponse);

        // When: 결제 취소 요청
        mockMvc.perform(post("/api/payments/cancel/{orderId}", payment.getOrderId())
                .param("cancelReason", "고객 변심"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200));

        // Then: 결제 상태와 실패 사유 확인
        Payment cancelledPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(cancelledPayment.getFailureReason()).isEqualTo("고객 변심");

        // Then: 포인트 차감 내역 확인 (충전 + 차감 총 2건)
        List<Point> pointHistory = pointRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(pointHistory).hasSize(2);

        Point usePoint = pointHistory.get(0); // 최신 기록 (차감)
        assertThat(usePoint.getPointType()).isEqualTo(PointType.USE);
        assertThat(usePoint.getAmount()).isEqualTo(20000);
        assertThat(usePoint.getDescription()).contains("결제 취소로 인한 포인트 차감");
    }


    @Test
    @WithMockUser(username = "paymentuser@test.com", roles = "USER")
    @DisplayName("여러 건 결제 내역 조회 시나리오")
    void 여러_건_결제_내역_조회_시나리오() throws Exception {
        // Given
        Payment payment1 = Payment.builder()
            .user(user)
            .orderId("ORDER_1")
            .orderName("테스트 충전 1")
            .amount(5000)
            .status(PaymentStatus.FAILED)
            .failureReason("결제 실패")
            .build();
        paymentRepository.save(payment1);

        Thread.sleep(10);

        Payment payment2 = Payment.builder()
            .user(user)
            .orderId("ORDER_2")
            .orderName("테스트 충전 2")
            .amount(10000)
            .status(PaymentStatus.CONFIRMED)
            .paymentKey("test_key_1")
            .build();
        paymentRepository.save(payment2);

        Thread.sleep(10);

        Payment payment3 = Payment.builder()
            .user(user)
            .orderId("ORDER_3")
            .orderName("테스트 충전 3")
            .amount(15000)
            .status(PaymentStatus.CONFIRMED)
            .paymentKey("test_key_0")
            .build();
        paymentRepository.save(payment3);

        // When & Then
        mockMvc.perform(get("/api/payments/history")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data[0].orderId").value("ORDER_3"))    // 가장 최신
            .andExpect(jsonPath("$.data[0].status").value("CONFIRMED"))   // payment3
            .andExpect(jsonPath("$.data[1].orderId").value("ORDER_2"))    // 중간
            .andExpect(jsonPath("$.data[1].status").value("CONFIRMED"))   // payment2
            .andExpect(jsonPath("$.data[2].orderId").value("ORDER_1"))    // 가장 오래됨
            .andExpect(jsonPath("$.data[2].status").value("FAILED"))      // payment1
            .andExpect(jsonPath("$.data.length()").value(3));
    }

    // JWT 토큰 발급 메서드 (필요 시 직접 구현 또는 무시)
    private String generateJwtToken(User user) {
        // 실제 테스트에서는 토큰 발급 로직 또는 mock security 적용
        return "Bearer dummy-token";
    }

    @Test
    @DisplayName("결제 최소 금액 검증 시나리오")
    void 결제_최소_금액_검증_시나리오() throws Exception {
        // Given: 최소 금액 미만 결제 요청
        PaymentRequestDto invalidRequest = new PaymentRequestDto(500, "최소 금액 미만 충전");

        // When & Then: 결제 준비 시 검증 실패
        mockMvc.perform(post("/api/payments/prepare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Then: 결제 기록이 생성되지 않았는지 확인
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 취소 시도 시나리오")
    void 존재하지_않는_주문_취소_시나리오() throws Exception {
        // Given: 존재하지 않는 주문 ID
        String nonExistentOrderId = "ORDER_20241231999999_NOTFOUND";

        // When & Then: 취소 시도 시 실패
        mockMvc.perform(post("/api/payments/cancel/{orderId}", nonExistentOrderId)
                .param("cancelReason", "테스트 취소"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PENDING 상태 결제 취소 시도 시나리오")
    void PENDING_상태_결제_취소_시도_시나리오() throws Exception {
        // Given: PENDING 상태의 결제
        PaymentRequestDto paymentRequest = new PaymentRequestDto(10000, "취소 불가 테스트");
        
        mockMvc.perform(post("/api/payments/prepare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());

        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        String orderId = payments.get(0).getOrderId();

        // When & Then: PENDING 상태에서 취소 시도 시 실패
        mockMvc.perform(post("/api/payments/cancel/{orderId}", orderId)
                .param("cancelReason", "PENDING 상태 취소 시도"))
                .andExpect(status().isBadRequest());
    }
}
