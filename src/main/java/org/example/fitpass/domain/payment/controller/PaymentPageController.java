package org.example.fitpass.domain.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentPageController {
    
    @GetMapping("/success")
    public String paymentSuccess() {
        return "forward:/payment-success.html";
    }
    
    @GetMapping("/fail")
    public String paymentFail() {
        return "forward:/payment-fail.html";
    }
    
    // 결제 페이지 접근
    @GetMapping("")
    public String paymentPage() {
        return "forward:/payment.html";
    }
}
