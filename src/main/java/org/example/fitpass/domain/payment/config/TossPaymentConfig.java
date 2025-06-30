package org.example.fitpass.domain.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TossPaymentConfig {
    
    @Value("${toss.payments.test-client-key}")
    private String clientKey;
    
    @Value("${toss.payments.test-secret-key}")
    private String secretKey;
    
    @Value("${toss.payments.success-url}")
    private String successUrl;
    
    @Value("${toss.payments.fail-url}")
    private String failUrl;
    
    public static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments";
    public static final String TOSS_CONFIRM_URL = TOSS_API_URL + "/confirm";
}
