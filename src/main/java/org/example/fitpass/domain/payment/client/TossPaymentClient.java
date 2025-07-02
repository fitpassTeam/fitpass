package org.example.fitpass.domain.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.payment.config.TossPaymentConfig;
import org.example.fitpass.domain.payment.dto.request.PaymentConfirmRequestDto;
import org.example.fitpass.domain.payment.dto.response.PaymentCancelResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class TossPaymentClient {
    
    private final TossPaymentConfig tossPaymentConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // 토스페이먼츠 결제 승인 API 호출
    public PaymentResponseDto confirmPayment(String paymentKey, String orderId, Integer amount) {
        try {
            String auth = encodeSecretKey(tossPaymentConfig.getSecretKey());

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + auth);
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            PaymentConfirmRequestDto request = new PaymentConfirmRequestDto(paymentKey, orderId, amount);
            HttpEntity<PaymentConfirmRequestDto> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                TossPaymentConfig.TOSS_CONFIRM_URL,
                entity,
                String.class
            );

            log.info("토스페이먼츠 결제 승인 성공 - paymentKey: {}, orderId: {}", paymentKey, orderId);
            return objectMapper.readValue(response.getBody(), PaymentResponseDto.class);

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 실패 - paymentKey: {}, orderId: {}", paymentKey, orderId, e);
            throw new BaseException(ExceptionCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }
    }

    // 토스페이먼츠 결제 조회 API 호출
    public PaymentResponseDto getPayment(String paymentKey) {
        try {
            String auth = encodeSecretKey(tossPaymentConfig.getSecretKey());

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + auth);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                TossPaymentConfig.TOSS_API_URL + "/" + paymentKey,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("토스페이먼츠 결제 조회 성공 - paymentKey: {}", paymentKey);
            return objectMapper.readValue(response.getBody(), PaymentResponseDto.class);

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 조회 실패 - paymentKey: {}", paymentKey, e);
            throw new BaseException(ExceptionCode.TOSS_PAYMENT_STATUS_FAILED);
        }
    }

    // 토스페이먼츠 결제 취소 API 호출
    public PaymentCancelResponseDto cancelPayment(String paymentKey, String cancelReason) {
        try {
            String auth = encodeSecretKey(tossPaymentConfig.getSecretKey());

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + auth);
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            // 취소 요청 본문
            String requestBody = String.format("{\"cancelReason\":\"%s\"}", cancelReason);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                TossPaymentConfig.TOSS_API_URL + "/" + paymentKey + "/cancel",
                entity,
                String.class
            );

            log.info("토스페이먼츠 결제 취소 성공 - paymentKey: {}, reason: {}", paymentKey, cancelReason);
            return objectMapper.readValue(response.getBody(), PaymentCancelResponseDto.class);

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 실패 - paymentKey: {}", paymentKey, e);
            throw new BaseException(ExceptionCode.TOSS_PAYMENT_CANCEL_FAILED);
        }
    }

    private String encodeSecretKey(String secretKey) {
        return Base64.getEncoder()
            .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }
}
