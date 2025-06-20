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
    POST_STATUS_NOT_ACCEPT(HttpStatus.BAD_REQUEST, "400", "정확한 상태를 입력해주세요."),
    POST_TYPE_NOT_ACCEPT(HttpStatus.BAD_REQUEST, "400", "정확한 종류를 입력해주세요."),
    DAILY_RECORD_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "400", "해당 날짜에 이미 일일 기록이 존재합니다."),
    WEIGHT_RECORD_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "400", "해당 날짜에 이미 체중 기록이 존재합니다."),
    WEIGHT_RECORD_NOT_FOUND(HttpStatus.BAD_REQUEST, "400", "체중 기록을 찾을 수 없습니다."),
    FITNESS_GOAL_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "400", "이미 완료된 목표는 수정할 수 없습니다."),
    FITNESS_GOAL_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "400", "이미 취소된 목표입니다."),
    FITNESS_GOAL_ALREADY_EXPIRED(HttpStatus.BAD_REQUEST, "400", "만료된 목표는 수정할 수 없습니다."),
    FITNESS_GOAL_WEIGHT_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "400", "만료되었거나 완료된 목표의 체중은 업데이트할 수 없습니다."),
    FITNESS_GOAL_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "400", "완료된 목표는 취소할 수 없습니다."),
    FITNESS_GOAL_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "400", "완료된 목표는 삭제할 수 없습니다."),
    VALID_STATUS(HttpStatus.BAD_REQUEST,"400","잘못된 상태 입니다."),
    RESERVATION_NOT_COMPLETED(HttpStatus.BAD_REQUEST,"400", "완료된 예약만 리뷰를 작성할 수 있습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,"400", "이미 해당 예약에 대한 리뷰가 존재합니다."),
    INVALID_GYM_MEMBERSHIP(HttpStatus.BAD_REQUEST, "400", "해당 체육관에 속한 이용권이 아닙니다."),
    ALREADY_STARTED(HttpStatus.BAD_REQUEST, "400", "이미 사용된 이용권입니다."),

    // 401 Unauthorized = 인증이 안될 때
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"401","유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"401","리프레시 토큰이 유효하지 않습니다."),
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED,"401", "유효하지 않는 JWT 서명입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "401", "비밀번호가 일치하지 않습니다."),
    INVALID_OLD_PASSWORD(HttpStatus.UNAUTHORIZED, "401", "기존 비밀번호가 일치하지 않습니다."),

    // 403 Forbidden = 권한이 없을 때
    NO_OWNER_AUTHORITY(HttpStatus.FORBIDDEN,"403", "사장의 권한이 없습니다."),
    NOT_GYM_OWNER(HttpStatus.FORBIDDEN,"403", "권한이 없습니다."),
    NOT_POST_OWNER(HttpStatus.FORBIDDEN,"403", "권한이 없습니다."),
    POST_NOT_AUTHOR(HttpStatus.FORBIDDEN,"403","게시물 작성자만 수정이 가능합니다."),
    NOTICE_ONLY_OWNER(HttpStatus.FORBIDDEN, "403", "공지사항은 관리자만 작성할 수 있습니다."),
    NO_ADMIN_AUTHORITY(HttpStatus.FORBIDDEN, "403", "관리자 권한이 필요합니다."),
    NOT_RESERVATION_OWNER(HttpStatus.FORBIDDEN, "403", "본인의 예약만 취소/수정/조회 할 수 있습니다."),
    NOT_WEIGHT_RECORD_OWNER(HttpStatus.FORBIDDEN, "403", "체중 기록에 접근 권한이 없습니다."),
    NOT_DAILY_RECORD_OWNER(HttpStatus.FORBIDDEN, "403", "일일 기록에 접근 권한이 없습니다."),
    NOT_FITNESS_GOAL_OWNER(HttpStatus.FORBIDDEN, "403", "해당 목표에 접근 권한이 없습니다."),
    INVALID_GYM_TRAINER_RELATION(HttpStatus.FORBIDDEN, "403", "트레이너가 해당 체육관에 속하지 않습니다."),
    NOT_BELONG_TO_GYM(HttpStatus.FORBIDDEN, "403", "해당 체육관의 이용권 아닙니다."),
    NOT_REVIEW_OWNER(HttpStatus.FORBIDDEN, "403", "리뷰 작성자가 아닙니다."),
    NOT_HAS_AUTHORITY(HttpStatus.FORBIDDEN,"403", "권한이 없습니다."),

    // 404 Not Found
    CANT_FIND_DATA(HttpStatus.NOT_FOUND,"404", "해당 데이터를 찾을 수 없습니다."),
    GYM_NOT_FOUND(HttpStatus.NOT_FOUND,"404","찾으시는 체육관이 존재하지 않습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND,"404","게시물이 존재하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "사용자를 찾을 수 없습니다."),
    TRAINER_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "트레이너를 찾을 수 없습니다."),
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "채팅방이 존재하지 않습니다."),
    FITNESS_GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "목표를 찾을 수 없습니다."),
    DAILY_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "일일 기록을 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "예약을 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "리뷰를 찾을 수 없습니다."),
    MEMBERSHIP_NOT_ACTIVE(HttpStatus.NOT_FOUND, "404", "활성화된 이용권을 찾을 수 없습니다."),
    MEMBERSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "이용권을 찾을 수 없습니다."),
    NOT_FOUND_PURCHASE(HttpStatus.NOT_FOUND, "404", "구매한 이용권을 찾을 수 없습니다."),

    // 409 Conflict = 서버와 충돌, 데이터가 이미 존재할때(400 보다 명확함)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,"409", "이미 등록된 회원입니다."),
    RESERVATION_TIME_CONFLICT(HttpStatus.CONFLICT, "409", "해당 시간에 이미 다른 예약이 있습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "409", "이미 존재하는 이메일입니다."),

    // 500 Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"500", "서버 내부 오류 혹은 예기치 못한 예외가 발생했습니다."),
    S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR,"500", "S3 업로드를 실패하였습니다."),
    S3_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR,"500", "S3 삭제를 실패하였습니다."),
    RESERVATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "409", "해당 시간에 이미 예약이 존재합니다."),
    RESERVATION_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "500", "예약 처리 중 인터럽트가 발생했습니다."),
    FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "500", "이미지 업로드에 실패했습니다.");
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
