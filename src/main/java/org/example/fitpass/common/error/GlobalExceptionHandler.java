package org.example.fitpass.common.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleBaseException(BaseException e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        
        // 에러 코드별로 로그 레벨 구분
        if (isSecurityRelatedError(e.getErrorCode())) {
            log.warn("[SECURITY ALERT] 보안 관련 예외 - CODE: {}, MESSAGE: {}, USER: {}, CLIENT: {}, PATH: {}", 
                    e.getErrorCode().getCode(), e.getMessage(), userInfo, clientInfo, request.getRequestURI());
        } else if (isCriticalBusinessError(e.getErrorCode())) {
            log.error("[BUSINESS CRITICAL] 중요 비즈니스 예외 - CODE: {}, MESSAGE: {}, USER: {}, CLIENT: {}, PATH: {}", 
                    e.getErrorCode().getCode(), e.getMessage(), userInfo, clientInfo, request.getRequestURI());
        } else {
            log.info("[BUSINESS ERROR] 일반 비즈니스 예외 - CODE: {}, MESSAGE: {}, USER: {}, CLIENT: {}, PATH: {}", 
                    e.getErrorCode().getCode(), e.getMessage(), userInfo, clientInfo, request.getRequestURI());
        }

        return buildErrorResponse(e.getErrorCode(), request.getRequestURI(), null, request);
    }

    // 인증 실패 예외 처리
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        
        log.warn("[SECURITY ALERT] 인증 실패 - MESSAGE: {}, CLIENT: {}, PATH: {}, REFERER: {}", 
                e.getMessage(), clientInfo, request.getRequestURI(), request.getHeader("Referer"));

        return buildErrorResponse(ExceptionCode.UNAUTHORIZED, request.getRequestURI(), null, request);
    }

    // 권한 없음 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        
        log.warn("[SECURITY ALERT] 접근 권한 없음 - MESSAGE: {}, USER: {}, CLIENT: {}, PATH: {}, METHOD: {}", 
                e.getMessage(), userInfo, clientInfo, request.getRequestURI(), request.getMethod());

        return buildErrorResponse(ExceptionCode.FORBIDDEN, request.getRequestURI(), null, request);
    }

    // Bean Validation 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Optional.ofNullable(error.getDefaultMessage()).orElse("Validation failed"),
                        "rejectedValue", String.valueOf(error.getRejectedValue())
                ))
                .collect(Collectors.toList());

        log.info("[VALIDATION ERROR] 입력값 검증 실패 - USER: {}, CLIENT: {}, PATH: {}, ERRORS: {}", 
                userInfo, clientInfo, request.getRequestURI(), 
                fieldErrors.stream().map(err -> err.get("field") + ":" + err.get("message")).collect(Collectors.joining(", ")));

        return buildErrorResponse(ExceptionCode.VALID_ERROR, request.getRequestURI(), fieldErrors, request);
    }

    // 필수 파라미터 누락 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        
        log.info("[PARAMETER ERROR] 필수 파라미터 누락 - PARAMETER: {}, TYPE: {}, USER: {}, CLIENT: {}, PATH: {}", 
                e.getParameterName(), e.getParameterType(), userInfo, clientInfo, request.getRequestURI());

        List<Map<String, String>> fieldErrors = List.of(Map.of(
                "field", e.getParameterName(),
                "message", "Required parameter is missing",
                "rejectedValue", "null"
        ));

        return buildErrorResponse(ExceptionCode.VALID_ERROR, request.getRequestURI(), fieldErrors, request);
    }

    // 파라미터 타입 불일치 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        
        log.info("[TYPE MISMATCH] 파라미터 타입 불일치 - PARAMETER: {}, VALUE: {}, EXPECTED_TYPE: {}, USER: {}, CLIENT: {}, PATH: {}", 
                e.getName(), e.getValue(), e.getRequiredType().getSimpleName(), userInfo, clientInfo, request.getRequestURI());

        List<Map<String, String>> fieldErrors = List.of(Map.of(
                "field", e.getName(),
                "message", "Invalid parameter type. Expected: " + e.getRequiredType().getSimpleName(),
                "rejectedValue", String.valueOf(e.getValue())
        ));

        return buildErrorResponse(ExceptionCode.VALID_ERROR, request.getRequestURI(), fieldErrors, request);
    }

    // JSON 파싱 실패 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();

        log.warn("[JSON PARSE ERROR] JSON 파싱 실패 - USER: {}, CLIENT: {}, PATH: {}, ERROR: {}", 
                userInfo, clientInfo, request.getRequestURI(), ex.getMessage());

        // JSON 파싱 중 InvalidFormatException이 포함된 경우, 상세 메시지 추출
        String detailMessage = "Request body is malformed or has incorrect types.";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            detailMessage = String.format("Invalid value for field '%s': expected type %s",
                    invalidFormatException.getPath().stream()
                            .map(ref -> ref.getFieldName())
                            .collect(Collectors.joining(".")),
                    invalidFormatException.getTargetType().getSimpleName()
            );
        }

        Map<String, String> errorDetail = Map.of(
                "field", "requestBody",
                "message", detailMessage,
                "rejectedValue", "malformed_json"
        );

        return buildErrorResponse(ExceptionCode.INVALID_JSON, request.getRequestURI(), List.of(errorDetail), request);
    }

    // 지원하지 않는 HTTP 메서드 예외 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        
        log.info("[METHOD NOT SUPPORTED] 지원하지 않는 HTTP 메서드 - METHOD: {}, SUPPORTED: {}, USER: {}, CLIENT: {}, PATH: {}", 
                e.getMethod(), e.getSupportedHttpMethods(), userInfo, clientInfo, request.getRequestURI());

        return buildErrorResponse(ExceptionCode.METHOD_NOT_ALLOWED, request.getRequestURI(), null, request);
    }

    // 404 예외 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        
        log.info("[NOT FOUND] 존재하지 않는 엔드포인트 - METHOD: {}, USER: {}, CLIENT: {}, PATH: {}", 
                e.getHttpMethod(), userInfo, clientInfo, e.getRequestURL());

        return buildErrorResponse(ExceptionCode.NOT_FOUND, request.getRequestURI(), null, request);
    }

    // 파일 업로드 크기 초과 예외 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        
        log.warn("[UPLOAD SIZE ERROR] 파일 업로드 크기 초과 - MAX_SIZE: {}, USER: {}, CLIENT: {}, PATH: {}", 
                e.getMaxUploadSize(), userInfo, clientInfo, request.getRequestURI());

        return buildErrorResponse(ExceptionCode.FILE_SIZE_EXCEEDED, request.getRequestURI(), null, request);
    }

    // 예상치 못한 시스템 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(
            Exception e, HttpServletRequest request) {
        String clientInfo = getClientInfo(request);
        String userInfo = getCurrentUserInfo();
        String errorId = generateErrorId();
        
        log.error("[CRITICAL ERROR] 예상치 못한 시스템 예외 - ERROR_ID: {}, USER: {}, CLIENT: {}, PATH: {}, ERROR: {}", 
                errorId, userInfo, clientInfo, request.getRequestURI(), e.getMessage(), e);

        // 에러 ID를 응답에 포함하여 사용자가 문의할 때 사용할 수 있도록 함
        return buildErrorResponseWithErrorId(ExceptionCode.INTERNAL_SERVER_ERROR, request.getRequestURI(), errorId, request);
    }

    // 클라이언트 정보 추출 (IP, User-Agent)
    private String getClientInfo(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("Unknown");
        return String.format("IP:%s, UserAgent:%s", clientIp, userAgent.length() > 100 ? userAgent.substring(0, 100) + "..." : userAgent);
    }

    // 실제 클라이언트 IP 주소 가져오기 (프록시 고려)
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    // 현재 인증된 사용자 정보 가져오기
    private String getCurrentUserInfo() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // 인증 정보 가져오기 실패 시 무시
        }
        return "Anonymous";
    }

    // 보안 관련 에러인지 확인
    private boolean isSecurityRelatedError(ExceptionCode errorCode) {
        String code = errorCode.name();
        return code.contains("AUTH") || 
               code.contains("SECURITY") ||
               code.contains("UNAUTHORIZED") ||
               code.contains("FORBIDDEN");
    }

    // 중요한 비즈니스 에러인지 확인
    private boolean isCriticalBusinessError(ExceptionCode errorCode) {
        String code = errorCode.name();
        return code.contains("PAYMENT") ||
               code.contains("POINT") ||
               code.contains("RESERVATION") ||
               code.contains("CRITICAL");
    }

    // 에러 응답 생성
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            ExceptionCode errorCode, String path, List<Map<String, String>> fieldErrors, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("status", errorCode.getHttpStatus().value());
        body.put("error", errorCode.getHttpStatus().getReasonPhrase());
        body.put("code", errorCode.getCode());
        body.put("message", errorCode.getMessage());
        body.put("path", path);
        body.put("timestamp", LocalDateTime.now());
        body.put("method", request.getMethod());
        
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            body.put("fieldErrors", fieldErrors);
        }
        
        return new ResponseEntity<>(body, errorCode.getHttpStatus());
    }

    // 시스템 에러용 에러 ID 포함 응답 생성
    private ResponseEntity<Map<String, Object>> buildErrorResponseWithErrorId(
            ExceptionCode errorCode, String path, String errorId, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("status", errorCode.getHttpStatus().value());
        body.put("error", errorCode.getHttpStatus().getReasonPhrase());
        body.put("code", errorCode.getCode());
        body.put("message", errorCode.getMessage());
        body.put("path", path);
        body.put("timestamp", LocalDateTime.now());
        body.put("method", request.getMethod());
        body.put("errorId", errorId); // 문의용 에러 ID
        
        return new ResponseEntity<>(body, errorCode.getHttpStatus());
    }

    // 고유한 에러 ID 생성
    private String generateErrorId() {
        return "ERR_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString((int) (Math.random() * 0xFFFF));
    }
}
