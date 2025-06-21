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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Base Exception 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleBaseException(BaseException e,
        HttpServletRequest request) {
        return buildErrorResponse(e.getErrorCode(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> Map.of(
                "field", error.getField(),
                "message", Optional.of(error.getDefaultMessage()).orElse("Validation failed")
            ))
            .collect(Collectors.toList());

        return buildErrorResponse(ExceptionCode.VALID_ERROR, request.getRequestURI(), fieldErrors);
    }


    private ResponseEntity<Map<String, Object>> buildErrorResponse(
        ExceptionCode errorCode, String path, List<Map<String, String>> fieldErrors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", errorCode.getHttpStatus().value());
        body.put("error", errorCode.getHttpStatus().getReasonPhrase());
        body.put("code", errorCode.getBaseCode());
        body.put("message", errorCode.getMessage());
        body.put("path", path);
        body.put("timestamp", LocalDateTime.now());
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            body.put("fieldErrors", fieldErrors);
        }
        return new ResponseEntity<>(body, errorCode.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("HttpMessageNotReadableException: {}", ex.getMessage(), ex);

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
            "message", detailMessage
        );

        return buildErrorResponse(ExceptionCode.INVALID_JSON, request.getRequestURI(), List.of(errorDetail));
    }

}

