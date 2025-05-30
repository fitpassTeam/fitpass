package org.example.fitpass.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor

public enum ExceptionCode implements BaseCode {

    // 400 Bad Request
    JWT_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST,"400", "JWT 토큰이 필요합니다."),
    VALID_ERROR(HttpStatus.BAD_REQUEST, "400","Validation 이 유효하지 않습니다"),

    // 401 Unauthorized = 인증이 안될 때
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED,"401", "유효하지 않는 JWT 서명입니다."),

    // 403 Forbidden = 권한이 없을 때
    NO_OWNER_AUTHORITY(HttpStatus.FORBIDDEN,"403", "사장의 권한이 없습니다."),

    // 404 Not Found
    CANT_FIND_DATA(HttpStatus.NOT_FOUND,"404", "해당 데이터를 찾을 수 없습니다."),

    // 409 Conflict = 서버와 충돌, 데이터가 이미 존재할때(400 보다 명확함)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,"409", "이미 등록된 회원입니다."),

    // 500 Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"500", "서버 내부 오류 혹은 예기치 못한 예외가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public int getBaseCode() {
        return httpStatus.value();
    }
}
