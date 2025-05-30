package org.example.fitpass.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor

public enum ExceptionCode implements BaseCode {

    // 400 Bad Request
    JWT_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST,"400", "JWT 토큰이 필요합니다."),
    VALID_ERROR(HttpStatus.BAD_REQUEST,"400", "Validation 이 유효하지 않습니다"),
    RESERVATION_STATUS_NOT_CHANGEABLE(HttpStatus.BAD_REQUEST, "400", "대기 상태의 예약만 변경이 가능합니다."),
    RESERVATION_CHANGE_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "400", "예약 2일 전까지만 변경이 가능합니다."),
    RESERVATION_TOO_EARLY(HttpStatus.BAD_REQUEST, "400", "예약은 2일 후부터 가능합니다."),
    RESERVATION_STATUS_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST, "400", "대기 중이거나 확정된 예약만 취소할 수 있습니다."),
    RESERVATION_CANCEL_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "400", "예약 2일 전까지만 취소가 가능합니다."),
    INSUFFICIENT_POINT_BALANCE(HttpStatus.BAD_REQUEST, "400", "포인트 잔액이 부족합니다."),

    // 401 Unauthorized = 인증이 안될 때
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED,"401", "유효하지 않는 JWT 서명입니다."),

    // 403 Forbidden = 권한이 없을 때
    NO_OWNER_AUTHORITY(HttpStatus.FORBIDDEN,"403", "사장의 권한이 없습니다."),
    NOT_GYM_OWNER(HttpStatus.FORBIDDEN,"403", "권한이 없습니다."),

    NOT_RESERVATION_OWNER(HttpStatus.FORBIDDEN, "403", "본인의 예약만 취소할 수 있습니다."),

    // 404 Not Found
    CANT_FIND_DATA(HttpStatus.NOT_FOUND,"404", "해당 데이터를 찾을 수 없습니다."),
    GYM_NOT_FOUND(HttpStatus.NOT_FOUND,"404","찾으시는 체육관이 존재하지 않습니다."),

    // 409 Conflict = 서버와 충돌, 데이터가 이미 존재할때(400 보다 명확함)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,"409", "이미 등록된 회원입니다."),
    RESERVATION_TIME_CONFLICT(HttpStatus.CONFLICT, "409", "해당 시간에 이미 다른 예약이 있습니다."),

    // 500 Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"500", "서버 내부 오류 혹은 예기치 못한 예외가 발생했습니다."),
    RESERVATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "409", "해당 시간에 이미 예약이 존재합니다.");

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
