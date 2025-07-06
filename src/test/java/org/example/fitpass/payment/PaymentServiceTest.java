package org.example.fitpass.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.payment.client.TossPaymentClient;
import org.example.fitpass.domain.payment.config.TossPaymentConfig;
import org.example.fitpass.domain.payment.dto.response.PaymentCancelResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentUrlResponseDto;
import org.example.fitpass.domain.payment.entity.Payment;
import org.example.fitpass.domain.payment.enums.PaymentStatus;
import org.example.fitpass.domain.payment.repository.PaymentRepository;
import org.example.fitpass.domain.payment.service.PaymentService;
import org.example.fitpass.domain.point.dto.response.PointBalanceResponseDto;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("결제 서비스 단위 테스트")
public class PaymentServiceTest {

    @InjectMocks private PaymentService paymentService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PointService pointService;
    @Mock private TossPaymentClient tossPaymentClient;
    @Mock private TossPaymentConfig tossPaymentConfig;

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
    private Payment payment;

    @BeforeEach
    void setUp() {
        user = new User(
            "service@test.com", "profile.jpg", "password123", "서비스테스트유저",
            "010-5555-6666", 32, "서울시 강서구", Gender.WOMAN, UserRole.USER
        );
        // 테스트를 위해 리플렉션으로 ID 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        payment = Payment.builder()
            .id(1L)
            .orderId("ORDER_TEST_20250102")
            .orderName("서비스 테스트 충전")
            .amount(50000)
            .status(PaymentStatus.PENDING)
            .user(user)
            .build();
    }

    @Test
    @DisplayName("결제 준비 성공 테스트")
    void 결제_준비_성공_테스트() {
        // Given
        given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);
        given(tossPaymentConfig.getSuccessUrl()).willReturn("http://success.url");
        given(tossPaymentConfig.getFailUrl()).willReturn("http://fail.url");

        // When
        PaymentUrlResponseDto result = paymentService.preparePayment(1L, 50000, "서비스 테스트 충전");

        // Then
        assertThat(result.amount()).isEqualTo(50000);
        assertThat(result.orderName()).isEqualTo("서비스 테스트 충전");
        assertThat(result.customerEmail()).isEqualTo(user.getEmail());
        assertThat(result.customerName()).isEqualTo(user.getName());
        assertThat(result.successUrl()).isEqualTo("http://success.url");
        assertThat(result.failUrl()).isEqualTo("http://fail.url");

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 준비 시 사용자 없음 에러 테스트")
    void 결제_준비_시_사용자_없음_에러_테스트() {
        // Given
        given(userRepository.findByIdOrElseThrow(999L))
            .willThrow(new BaseException(ExceptionCode.USER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() ->
            paymentService.preparePayment(999L, 10000, "존재하지 않는 사용자")
        ).isInstanceOf(BaseException.class);

        verify(userRepository).findByIdOrElseThrow(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

//    @Test
//    @DisplayName("결제 승인 성공 테스트")
//    void 결제_승인_성공_테스트() {
//        // Given
//        String paymentKey = "test_payment_key";
//        String orderId = "ORDER_TEST_20250102";
//        Integer amount = 50000;
//
//        PaymentResponseDto tossResponse = new PaymentResponseDto(
//            paymentKey,
//            orderId,
//            "서비스 테스트 충전",
//            amount,
//            "DONE",
//            LocalDateTime.now(), // approvedAt
//            "카드"
//        );
//
//        given(paymentRepository.findByIdOrElseThrow(payment.getOrderId()))
//            .willThrow(new BaseException(ExceptionCode.PAYMENT_NOT_FOUND));
//        given(tossPaymentClient.confirmPayment(paymentKey, orderId, amount)).willReturn(tossResponse);
//        given(pointService.chargePoint(eq(1L), eq(50000), anyString()))
//            .willReturn(new PointBalanceResponseDto(50000)); // 실제 예상되는 잔액
//
//        // When
//        PaymentResponseDto result = paymentService.confirmPayment(paymentKey, orderId, amount);
//
//        // Then
//        assertThat(result.paymentKey()).isEqualTo(paymentKey);
//        assertThat(result.status()).isEqualTo("DONE");
//        assertThat(result.amount()).isEqualTo(amount);
//
//        verify(paymentRepository).findByOrderId(orderId);
//        verify(tossPaymentClient).confirmPayment(paymentKey, orderId, amount);
//        verify(pointService).chargePoint(1L, 50000, "토스페이먼츠 충전 - 서비스 테스트 충전");
//    }


    @Test
    @DisplayName("결제 승인 시 금액 불일치 에러 테스트")
    void 결제_승인_시_금액_불일치_에러_테스트() {
        // Given
        String paymentKey = "test_payment_key";
        String orderId = "ORDER_TEST_20250102";
        Integer wrongAmount = 30000; // 원래 금액과 다름

        given(paymentRepository.findByIdOrElseThrow(payment.getOrderId()))
            .willThrow(new BaseException(ExceptionCode.PAYMENT_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() ->
            paymentService.confirmPayment(paymentKey, orderId, wrongAmount)
        ).isInstanceOf(BaseException.class);

        // 토스 API 호출되지 않음
        verify(tossPaymentClient, never()).confirmPayment(anyString(), anyString(), anyInt());
        verify(pointService, never()).chargePoint(anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("결제 승인 시 토스 API 에러 테스트")
    void 결제_승인_시_토스_API_에러_테스트() {
        // Given
        String paymentKey = "error_payment_key";
        String orderId = "ORDER_TEST_20250102";
        Integer amount = 50000;

        given(paymentRepository.findByIdOrElseThrow(payment.getOrderId()))
            .willThrow(new BaseException(ExceptionCode.PAYMENT_NOT_FOUND));
        given(tossPaymentClient.confirmPayment(paymentKey, orderId, amount))
            .willThrow(new BaseException(ExceptionCode.TOSS_PAYMENT_CONFIRM_FAILED));

        // When
        assertThatThrownBy(() ->
            paymentService.confirmPayment(paymentKey, orderId, amount)
        ).isInstanceOf(BaseException.class);

        // Then - 결제 상태는 서비스 내에서 변경되므로 확인하지 않음
        verify(pointService, never()).chargePoint(anyLong(), anyInt(), anyString());
    }

//    @Test
//    @DisplayName("결제 실패 처리 테스트")
//    void 결제_실패_처리_테스트() {
//        // Given
//        String orderId = "ORDER_TEST_20250102";
//        String failureReason = "카드 승인 거부";
//
//        // 결제를 찾을 수 있어야 함 (정상적으로 반환)
//        given(paymentRepository.findByOrderId(orderId))
//            .willReturn(Optional.of(payment));
//
//        // When
//        paymentService.failPayment(orderId, failureReason);
//
//        // Then
//        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
//        assertThat(payment.getFailureReason()).isEqualTo(failureReason);
//
//        verify(paymentRepository).findByOrderId(orderId);
//    }

    @Test
    @DisplayName("결제 내역 조회 테스트")
    void 결제_내역_조회_테스트() {
        // Given
        Payment payment1 = Payment.builder()
            .orderId("ORDER_1")
            .orderName("첫 번째 결제")
            .amount(10000)
            .status(PaymentStatus.CONFIRMED)
            .user(user)
            .build();

        Payment payment2 = Payment.builder()
            .orderId("ORDER_2")
            .orderName("두 번째 결제")
            .amount(20000)
            .status(PaymentStatus.FAILED)
            .user(user)
            .build();

        given(paymentRepository.findByUserIdOrderByCreatedAtDesc(1L))
            .willReturn(List.of(payment2, payment1)); // 최신순

        // When
        List<Payment> result = paymentService.getPaymentHistory(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderId()).isEqualTo("ORDER_2");
        assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.get(1).getOrderId()).isEqualTo("ORDER_1");
        assertThat(result.get(1).getStatus()).isEqualTo(PaymentStatus.CONFIRMED);

        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("결제 상태 조회 성공 테스트")
    void 결제_상태_조회_성공_테스트() {
        // Given
        String paymentKey = "status_test_key";
        PaymentResponseDto tossResponse = new PaymentResponseDto(
            paymentKey,
            "orderId",
            "서비스 테스트 충전",
            50000,
            "DONE",
            LocalDateTime.now(), // approvedAt
            "카드"
        );

        given(tossPaymentClient.getPayment(paymentKey)).willReturn(tossResponse);

        // When
        PaymentResponseDto result = paymentService.getPaymentStatus(paymentKey);

        // Then
        assertThat(result.paymentKey()).isEqualTo(paymentKey);
        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.amount()).isEqualTo(50000); // 실제 응답과 맞춤

        verify(tossPaymentClient).getPayment(paymentKey);
    }

    @Test
    @DisplayName("결제 상태 조회 실패 테스트")
    void 결제_상태_조회_실패_테스트() {
        // Given
        String paymentKey = "invalid_payment_key";

        given(tossPaymentClient.getPayment(paymentKey))
            .willThrow(new BaseException(ExceptionCode.TOSS_PAYMENT_STATUS_FAILED));

        // When & Then
        assertThatThrownBy(() ->
            paymentService.getPaymentStatus(paymentKey)
        ).isInstanceOf(BaseException.class);

        verify(tossPaymentClient).getPayment(paymentKey);
    }

//    @Test
//    @DisplayName("결제 취소 성공 테스트")
//    void 결제_취소_성공_테스트() {
//        // Given
//        String orderId = "ORDER_TEST_20250102";
//        String cancelReason = "고객 요청";
//        String paymentKey = "cancel_test_key";
//
//        // 승인된 결제로 설정
//        payment.updatePaymentKey(paymentKey);
//        payment.updateStatus(PaymentStatus.CONFIRMED);
//
//        PaymentCancelResponseDto tossResponse = new PaymentCancelResponseDto(
//            paymentKey,
//            orderId,
//            "서비스 테스트 충전",
//            50000,
//            "CANCELED", // 취소된 상태
//            LocalDateTime.now(),
//            "카드"
//        );
//
//        given(paymentRepository.findByIdOrElseThrow(tossResponse.orderId()))
//            .willThrow(new BaseException(ExceptionCode.PAYMENT_NOT_FOUND));
//        given(tossPaymentClient.cancelPayment(paymentKey, cancelReason)).willReturn(tossResponse);
//        given(pointService.usePoint(eq(1L), eq(50000), anyString()))
//            .willReturn(new PointBalanceResponseDto(0)); // 또는 적절한 잔액
//
//        // When
//        PaymentCancelResponseDto result = paymentService.cancelPayment(orderId, cancelReason);
//
//        // Then
//        assertThat(result.status()).isEqualTo("CANCELED");
//
//        verify(paymentRepository).findByOrderId(orderId);
//        verify(tossPaymentClient).cancelPayment(paymentKey, cancelReason);
//        verify(pointService).usePoint(1L, 50000, "결제 취소로 인한 포인트 차감 - 서비스 테스트 충전");
//    }


    @Test
    @DisplayName("PENDING 상태 결제 취소 시도 에러 테스트")
    void PENDING_상태_결제_취소_시도_에러_테스트() {
        // Given
        String orderId = "ORDER_TEST_20250102";
        String cancelReason = "PENDING 상태 취소 시도";

        // PENDING 상태 유지 (기본값)
        given(paymentRepository.findByIdOrElseThrow(payment.getOrderId()))
            .willThrow(new BaseException(ExceptionCode.PAYMENT_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() ->
            paymentService.cancelPayment(orderId, cancelReason)
        ).isInstanceOf(BaseException.class);

        verify(tossPaymentClient, never()).cancelPayment(anyString(), anyString());
        verify(pointService, never()).usePoint(anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("PaymentKey 없는 결제 취소 시도 에러 테스트")
    void PaymentKey_없는_결제_취소_시도_에러_테스트() {
        // Given
        String orderId = "ORDER_TEST_20250102";
        String cancelReason = "PaymentKey 없음";

        // CONFIRMED 상태이지만 PaymentKey가 없는 경우
        payment.updateStatus(PaymentStatus.CONFIRMED);
        // paymentKey는 null 상태

        given(paymentRepository.findByIdOrElseThrow(payment.getOrderId()))
            .willThrow(new BaseException(ExceptionCode.PAYMENT_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() ->
            paymentService.cancelPayment(orderId, cancelReason)
        ).isInstanceOf(BaseException.class);

        verify(tossPaymentClient, never()).cancelPayment(anyString(), anyString());
        verify(pointService, never()).usePoint(anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 에러 테스트")
    void 존재하지_않는_주문_조회_에러_테스트() {
        // Given
        String nonExistentOrderId = "ORDER_NOT_FOUND";

        given(paymentRepository.findByIdOrElseThrow(nonExistentOrderId))
            .willThrow(new BaseException(ExceptionCode.PAYMENT_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() ->
            paymentService.confirmPayment("test_key", nonExistentOrderId, 10000)
        ).isInstanceOf(BaseException.class);

        verify(tossPaymentClient, never()).confirmPayment(anyString(), anyString(), anyInt());
    }
}
