package org.example.fitpass.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseCode {

    // 1000: Auth 성공 코드
    SIGNUP_SUCCESS(HttpStatus.CREATED, "1001", "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "1002", "로그인에 성공하였습니다."),
    REISSUE_SUCCESS(HttpStatus.OK, "1003", "토큰이 재발급되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "1004", "로그아웃이 완료되었습니다."),

    // 2000: User 성공 코드
    USER_GET_SUCCESS(HttpStatus.OK, "2001", "유저 조회 성공"),
    USER_DELETE_SUCCESS(HttpStatus.OK, "2002", "유저 삭제 성공"),
    USER_BOOK_LIST_SUCCESS(HttpStatus.OK, "2003", "대여한 도서 목록 조회 성공"),
    USER_UPDATE_SUCCESS(HttpStatus.OK, "2004", "유저 수정 성공"),
    USER_LIKED_LIST_SUCCESS(HttpStatus.OK, "2005", "좋아요한 도서 목록 조회 성공"),


    // 3000: Point 성공 코드
    POINT_CHARGE_SUCCESS(HttpStatus.OK, "3001", "포인트 충전이 완료되었습니다."),
    POINT_USE_SUCCESS(HttpStatus.OK, "3002", "포인트 사용이 완료되었습니다."),
    POINT_REFUND_SUCCESS(HttpStatus.OK, "3003", "포인트 환불이 완료되었습니다."),
    POINT_CASH_OUT_SUCCESS(HttpStatus.OK, "3004", "포인트 현금화가 완료되었습니다."),
    POINT_BALANCE_GET_SUCCESS(HttpStatus.OK, "3005", "포인트 잔액 조회가 완료되었습니다."),
    POINT_HISTORY_GET_SUCCESS(HttpStatus.OK, "3006", "포인트 이력 조회가 완료되었습니다."),

    // 4000: Reservation 성공 코드
    RESERVATION_CREATE_SUCCESS(HttpStatus.CREATED, "4001", "예약이 완료되었습니다."),
    RESERVATION_UPDATE_SUCCESS(HttpStatus.OK, "4002", "예약 수정이 완료되었습니다."),
    RESERVATION_CANCEL_WITH_REFUND_SUCCESS(HttpStatus.OK, "4003", "예약 취소가 완료되었습니다. 포인트가 환불되었습니다."),
    RESERVATION_GET_SUCCESS(HttpStatus.OK, "4004", "예약 조회가 완료되었습니다."),
    AVAILABLE_TIMES_GET_SUCCESS(HttpStatus.OK, "4005", "예약 가능 시간 조회가 완료되었습니다."),
    TRAINER_RESERVATION_LIST_SUCCESS(HttpStatus.OK, "4006", "트레이너 예약 목록 조회가 완료되었습니다."),
    USER_RESERVATION_LIST_SUCCESS(HttpStatus.OK, "4007", "사용자 예약 목록 조회가 완료되었습니다.");

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
