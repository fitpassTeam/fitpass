package org.example.fitpass.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.fitpass.common.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* org.example.fitpass.domain..controller..*(..))")
    public void controllerAPIs() {}

    @Around("controllerAPIs()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // HTTP 요청 정보 가져오기
        HttpServletRequest request = getCurrentHttpRequest();

        String method = request != null ? request.getMethod() : "UNKNOWN";
        String url = request != null ? request.getRequestURI() : "UNKNOWN";
        String queryParams = request != null ? request.getQueryString() : null;
        String ip = request != null ? getClientIpAddress(request) : "UNKNOWN";
        String sessionId = request != null ? request.getRequestedSessionId() : "UNKNOWN";

        // Spring Security에서 사용자 정보 가져오기
        String user = getCurrentUser();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // URL에 쿼리 파라미터 추가
        String fullUrl = url;
        if (queryParams != null && !queryParams.isEmpty()) {
            fullUrl = url + "?" + queryParams;
        }

        Object result;
        int statusCode = 200; // 기본 상태 코드

        try {
            result = joinPoint.proceed(); // 실제 메서드 실행

            // ResponseEntity에서 상태 코드 가져오기
            if (result instanceof ResponseEntity) {
                statusCode = ((ResponseEntity<?>) result).getStatusCode().value();
            }

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();

            // 에러 로그 - FitPass 프로젝트 스타일로 수정
            logger.error("[API ERROR] USER: {}, SESSION: {}, {} {}, IP: {}, METHOD: {}.{}, " +
                    "ERROR: {}, MESSAGE: {}, TIME: {}ms",
                user, sessionId, method, fullUrl, ip, className, methodName,
                e.getClass().getSimpleName(), e.getMessage(), (endTime - startTime));

            throw e;
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 성공 로그 - 실행 시간에 따라 다른 레벨 사용
        String performanceLevel = getPerformanceLevel(executionTime);

        logger.info("[API SUCCESS] USER: {}, SESSION: {}, {} {}, IP: {}, " +
                "STATUS: {}, TIME: {}ms, PERFORMANCE: {}",
            user, sessionId, method, fullUrl, ip, statusCode, executionTime, performanceLevel);

        return result;
    }

    // 현재 HTTP 요청 가져오기
    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    // Spring Security에서 현재 사용자 정보 가져오기
    private String getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return "Anonymous";
            }

            Object principal = authentication.getPrincipal();

            // CustomUserDetails에서 사용자 정보 추출
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                return userDetails.getUsername(); // 이메일이 username으로 사용됨
            }

            // OAuth2 사용자인 경우
            if (principal instanceof String) {
                return (String) principal;
            }

            return authentication.getName();

        } catch (Exception e) {
            return "Anonymous";
        }
    }

    // 실제 클라이언트 IP 주소 가져오기 (프록시 고려)
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 첫 번째 IP 사용 (여러 개인 경우)
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    // 실행 시간에 따른 성능 레벨 반환
    private String getPerformanceLevel(long executionTime) {
        if (executionTime < 100) {
            return "EXCELLENT"; // 매우 빠름
        } else if (executionTime < 500) {
            return "GOOD"; // 빠름
        } else if (executionTime < 1000) {
            return "NORMAL"; // 보통
        } else if (executionTime < 3000) {
            return "SLOW"; // 느림
        } else {
            return "CRITICAL"; // 매우 느림
        }
    }
}