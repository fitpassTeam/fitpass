package org.example.fitpass.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.payment.client.TossPaymentClient;
import org.example.fitpass.domain.payment.dto.response.PaymentCancelResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentUrlResponseDto;
import org.example.fitpass.domain.payment.entity.Payment;
import org.example.fitpass.domain.payment.enums.PaymentStatus;
import org.example.fitpass.domain.payment.repository.PaymentRepository;
import org.example.fitpass.domain.payment.service.PaymentService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("결제 통합 테스트 (외부 API 모킹)")
public class PaymentIntegrationTest {

    @Autowired private PaymentService paymentService;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private UserRepository userRepository;

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
    
    @MockBean private TossPaymentClient tossPaymentClient;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(
            "integration@test.com", "profile.jpg", "password123", "통합테스트유저",
            "010-9999-8888", 30, "서울시 송파구", Gender.WOMAN, UserRole.USER
        ));
    }

    @Test
    @DisplayName("결제 준비 → 승인 → 포인트 충전 통합 시나리오")
    void 결제_통합_성공_시나리오() {
        // Given: 토스 API 응답 모킹
        PaymentResponseDto mockTossResponse = new PaymentResponseDto(
            "test_payment_key", "ORDER_TEST_123", "프리미엄 포인트 충전", 15000, "DONE",
            java.time.LocalDateTime.now(), // approvedAt 추가
            "카드"
        );
        given(tossPaymentClient.confirmPayment(anyString(), anyString(), anyInt()))
            .willReturn(mockTossResponse);

        // When 1: 결제 준비
        PaymentUrlResponseDto prepareResponse = paymentService.preparePayment(
            user.getId(), 15000, "프리미엄 포인트 충전"
        );

        // Then 1: 결제 준비 결과 검증
        assertThat(prepareResponse.amount()).isEqualTo(15000);
        assertThat(prepareResponse.orderName()).isEqualTo("프리미엄 포인트 충전");
        assertThat(prepareResponse.customerEmail()).isEqualTo(user.getEmail());
        assertThat(prepareResponse.customerName()).isEqualTo(user.getName());

        // DB에 PENDING 상태로 저장 확인
        Payment pendingPayment = paymentRepository.findByOrderId(prepareResponse.orderId()).orElseThrow();
        assertThat(pendingPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(pendingPayment.getAmount()).isEqualTo(15000);

        // When 2: 결제 승인
        PaymentResponseDto confirmResponse = paymentService.confirmPayment(
            "test_payment_key", 
            prepareResponse.orderId(), 
            15000
        );

        // Then 2: 결제 승인 결과 검증
        assertThat(confirmResponse.paymentKey()).isEqualTo("test_payment_key");
        assertThat(confirmResponse.status()).isEqualTo("DONE");
        assertThat(confirmResponse.amount()).isEqualTo(15000);

        // DB에 CONFIRMED 상태로 업데이트 확인
        Payment confirmedPayment = paymentRepository.findByOrderId(prepareResponse.orderId()).orElseThrow();
        assertThat(confirmedPayment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(confirmedPayment.getPaymentKey()).isEqualTo("test_payment_key");
        assertThat(confirmedPayment.getMethod()).isEqualTo("카드");

        // 포인트 충전 확인
        List<Point> pointHistory = pointRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(pointHistory).hasSize(1);
        
        Point chargedPoint = pointHistory.get(0);
        assertThat(chargedPoint.getPointType()).isEqualTo(PointType.CHARGE);
        assertThat(chargedPoint.getAmount()).isEqualTo(15000);
        assertThat(chargedPoint.getDescription()).contains("토스페이먼츠 충전");

        // 토스 API 호출 검증
        verify(tossPaymentClient).confirmPayment("test_payment_key", prepareResponse.orderId(), 15000);
    }

    @Test
    @DisplayName("토스 API 에러로 인한 결제 실패 시나리오")
    void 토스_API_에러_결제_실패_시나리오() {
        // Given: 결제 준비
        PaymentUrlResponseDto prepareResponse = paymentService.preparePayment(
            user.getId(), 10000, "API 에러 테스트"
        );

        // 토스 API 에러 모킹
        given(tossPaymentClient.confirmPayment(anyString(), anyString(), anyInt()))
            .willThrow(new BaseException(ExceptionCode.TOSS_PAYMENT_CONFIRM_FAILED)); // API 에러 시뮬레이션

        // When & Then: 결제 승인 실패
        assertThatThrownBy(() -> 
            paymentService.confirmPayment("error_payment_key", prepareResponse.orderId(), 10000)
        ).isInstanceOf(BaseException.class);

        // 결제 상태가 FAILED로 업데이트되었는지 확인
        Payment failedPayment = paymentRepository.findByOrderId(prepareResponse.orderId()).orElseThrow();
        assertThat(failedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(failedPayment.getFailureReason()).isNotNull();

        // 포인트는 충전되지 않았는지 확인
        List<Point> pointHistory = pointRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(pointHistory).isEmpty();
    }

    @Test
    @DisplayName("결제 금액 불일치 시나리오")
    void 결제_금액_불일치_시나리오() {
        // Given: 결제 준비
        PaymentUrlResponseDto prepareResponse = paymentService.preparePayment(
            user.getId(), 10000, "금액 불일치 테스트"
        );

        // When & Then: 다른 금액으로 승인 시도
        assertThatThrownBy(() -> 
            paymentService.confirmPayment("test_key", prepareResponse.orderId(), 20000) // 다른 금액
        ).isInstanceOf(BaseException.class);

        // 결제 상태는 여전히 PENDING
        Payment payment = paymentRepository.findByOrderId(prepareResponse.orderId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        // 토스 API는 호출되지 않음
        verify(tossPaymentClient, never()).confirmPayment(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("결제 취소 통합 시나리오")
    void 결제_취소_통합_시나리오() {
        // Given: 성공한 결제 준비
        PaymentUrlResponseDto prepareResponse = paymentService.preparePayment(
            user.getId(), 25000, "취소 통합 테스트"
        );

        // 결제 승인 모킹
        PaymentResponseDto mockConfirmResponse = new PaymentResponseDto(
            "cancel_test_key", prepareResponse.orderId(), "취소 통합 테스트", 25000, "DONE",
            java.time.LocalDateTime.now(), // approvedAt 추가
            "카드"
        );
        given(tossPaymentClient.confirmPayment(anyString(), anyString(), anyInt()))
            .willReturn(mockConfirmResponse);

        // 결제 취소 모킹
        PaymentCancelResponseDto mockCancelResponse = new PaymentCancelResponseDto(
            "cancel_test_key", prepareResponse.orderId(), "취소 통합 테스트", 25000, "CANCELED",
            java.time.LocalDateTime.now(), // cancelledAt 추가
            "카드"
        );
        given(tossPaymentClient.cancelPayment(anyString(), anyString()))
            .willReturn(mockCancelResponse);

        // 결제 승인
        paymentService.confirmPayment("cancel_test_key", prepareResponse.orderId(), 25000);

        // When: 결제 취소
        PaymentCancelResponseDto cancelResponse = paymentService.cancelPayment(
            prepareResponse.orderId(), "고객 요청"
        );

        // Then: 취소 결과 검증
        assertThat(cancelResponse.status()).isEqualTo("CANCELED");

        // DB 상태 확인
        Payment cancelledPayment = paymentRepository.findByOrderId(prepareResponse.orderId()).orElseThrow();
        assertThat(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(cancelledPayment.getFailureReason()).isEqualTo("고객 요청");

        // 포인트 내역 확인 (충전 + 차감)
        List<Point> pointHistory = pointRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(pointHistory).hasSize(2);
        
        Point usePoint = pointHistory.get(0); // 최신 기록 (차감)
        assertThat(usePoint.getPointType()).isEqualTo(PointType.USE);
        assertThat(usePoint.getAmount()).isEqualTo(25000);

        // API 호출 검증
        verify(tossPaymentClient).confirmPayment("cancel_test_key", prepareResponse.orderId(), 25000);
        verify(tossPaymentClient).cancelPayment("cancel_test_key", "고객 요청");
    }

    @Test
    @DisplayName("결제 상태 조회 통합 시나리오")
    void 결제_상태_조회_통합_시나리오() {
        // Given: 토스 API 상태 조회 모킹
        PaymentResponseDto mockStatusResponse = new PaymentResponseDto(
            "status_test_key", "ORDER_STATUS_TEST", "상태 조회 테스트", 12000, "DONE",
            java.time.LocalDateTime.now(), // approvedAt 추가
            "카드"
        );
        given(tossPaymentClient.getPayment("status_test_key"))
            .willReturn(mockStatusResponse);

        // When: 결제 상태 조회
        PaymentResponseDto statusResponse = paymentService.getPaymentStatus("status_test_key");

        // Then: 결과 검증
        assertThat(statusResponse.paymentKey()).isEqualTo("status_test_key");
        assertThat(statusResponse.status()).isEqualTo("DONE");
        assertThat(statusResponse.amount()).isEqualTo(12000);

        // API 호출 검증
        verify(tossPaymentClient).getPayment("status_test_key");
    }

    @Test
    @DisplayName("동시 결제 요청 시나리오")
    void 동시_결제_요청_시나리오() {
        // Given: 동일 사용자의 여러 결제 요청
        PaymentUrlResponseDto response1 = paymentService.preparePayment(
            user.getId(), 5000, "첫 번째 충전"
        );
        
        PaymentUrlResponseDto response2 = paymentService.preparePayment(
            user.getId(), 8000, "두 번째 충전"
        );

        PaymentUrlResponseDto response3 = paymentService.preparePayment(
            user.getId(), 12000, "세 번째 충전"
        );

        // Then: 각각 고유한 주문 ID 생성 확인
        assertThat(response1.orderId()).isNotEqualTo(response2.orderId());
        assertThat(response2.orderId()).isNotEqualTo(response3.orderId());
        assertThat(response1.orderId()).isNotEqualTo(response3.orderId());

        // DB에 3개의 결제 기록 생성 확인
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(payments).hasSize(3);
        assertThat(payments).allMatch(p -> p.getStatus() == PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("결제 내역 조회 시나리오")
    void 결제_내역_조회_시나리오() {
        // Given: 다양한 상태의 결제 생성
        PaymentUrlResponseDto response1 = paymentService.preparePayment(
            user.getId(), 10000, "성공 결제"
        );
        
        PaymentUrlResponseDto response2 = paymentService.preparePayment(
            user.getId(), 20000, "실패 결제"
        );

        // 첫 번째 결제 성공 처리
        Payment payment1 = paymentRepository.findByOrderId(response1.orderId()).orElseThrow();
        payment1.updatePaymentKey("success_key");
        payment1.updateStatus(PaymentStatus.CONFIRMED);
        payment1.updateMethod("카드");

        // 두 번째 결제 실패 처리
        paymentService.failPayment(response2.orderId(), "카드 승인 실패");

        // When: 결제 내역 조회
        List<Payment> paymentHistory = paymentService.getPaymentHistory(user.getId());

        // Then: 결과 검증
        assertThat(paymentHistory).hasSize(2);
        assertThat(paymentHistory.get(0).getStatus()).isEqualTo(PaymentStatus.FAILED); // 최신순
        assertThat(paymentHistory.get(1).getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
    }
}
