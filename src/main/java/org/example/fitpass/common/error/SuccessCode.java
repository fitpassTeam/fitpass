package org.example.fitpass.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor

public enum SuccessCode implements BaseCode {

    // 1000: auth 성공 코드
    SIGNUP_SUCCESS(HttpStatus.CREATED, "1001", "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "1002", "로그인에 성공하였습니다."),
    REISSUE_SUCCESS(HttpStatus.OK, "1003", "토큰이 재발급되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "1004", "로그아웃이 완료되었습니다."),
    PASSWORD_MACTH_SUCCESS(HttpStatus.OK, "1005", "비밀번호가 일치합니다."),

    //2000: User 성공 코드
    USER_GET_SUCCESS(HttpStatus.OK, "2001", "유저 조회 성공"),
    USER_UPDATE_SUCCESS(HttpStatus.OK, "2002", "유저 수정 성공"),
    USER_PHONE_EDIT_SUCCESS(HttpStatus.OK, "2003", "전화번호 수정 성공"),
    USER_PASSWORD_EDIT_SUCCESS(HttpStatus.OK, "2004", "비밀번호 수정 성공"),
    OWNER_UPGRADE_REQUEST_SUCCESS(HttpStatus.OK, "2005", "사업자 승급 신청이 완료되었습니다."),
    OWNER_UPGRADE_APPROVE_SUCCESS(HttpStatus.OK, "2006", "사업자 승급이 승인되었습니다."),
    OWNER_UPGRADE_REJECT_SUCCESS(HttpStatus.OK, "2007", "사업자 승급이 거절되었습니다."),
    PENDING_REQUESTS_GET_SUCCESS(HttpStatus.OK, "2008", "승인 대기 목록 조회가 완료되었습니다."),
    USER_PROFILE_IMAGE_UPDATE_SUCCESS(HttpStatus.OK, "2009", "프로필 이미지 업데이트가 완료되었습니다."),

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
    USER_RESERVATION_LIST_SUCCESS(HttpStatus.OK, "4007", "사용자 예약 목록 조회가 완료되었습니다."),
    RESERVATION_CONFIRM_SUCCESS(HttpStatus.OK, "4008", "예약 승인이 완료되었습니다."),
    RESERVATION_REJECT_SUCCESS(HttpStatus.OK, "4009", "예약 거부가 완료되었습니다."),

    // 5000: Gym 성공 코드
    GYM_POST_SUCCESS(HttpStatus.OK, "5001", "체육관 등록 완료되었습니다."),
    GYM_FIND_SUCCESS(HttpStatus.OK, "5002", "체육관 상세조회 완료되었습니다."),
    GYM_FIND_ALL_SUCCESS(HttpStatus.OK, "5003", "체육관 전체조회 완료되었습니다."),
    GYM_EDIT_PHOTO_SUCCESS(HttpStatus.OK, "5004", "체육관 사진 수정 완료되었습니다."),
    GYM_EDIT_INFO_SUCCESS(HttpStatus.OK, "5005", "체육관 정보 수정 완료되었습니다."),
    GYM_DELETE_SUCCESS(HttpStatus.OK, "5006", "체육관이 삭제 처리되었습니다."),
    GYM_SEARCH_SUCCESS(HttpStatus.OK, "5007", "체육관 검색이 완료되었습니다."),
    GYM_RATING_GET_SUCCESS(HttpStatus.OK, "5008", "체육관 평점 검색이 완료되었습니다."),
    PENDING_GYM_REQUESTS_GET_SUCCESS(HttpStatus.OK, "5009", "승인 대기 체육관 목록 조회가 완료되었습니다."),
    GYM_APPROVE_SUCCESS(HttpStatus.OK, "5010", "체육관 승인이 완료되었습니다."),
    GYM_REJECT_SUCCESS(HttpStatus.OK, "5011", "체육관 승인이 거절되었습니다."),
    GYM_REQUEST_POST_SUCCESS(HttpStatus.OK, "5012", "체육관 등록 신청이 완료되었습니다."),

    // 6000 trainer 성공 코드
    POST_TRAINER_SUCCESS(HttpStatus.CREATED, "6001", "트레이너 생성이 완료되었습니다."),
    GET_TRAINER_SUCCESS(HttpStatus.OK, "6002", "트레이너 조회"),
    PATCH_TRAINER_SUCCESS(HttpStatus.OK, "6003", "트레이너 정보 수정이 완료되었습니다."),
    DELETE_TRAINER_SUCCESS(HttpStatus.OK, "6004", "트레이너가 삭제되었습니다"),
    PATCH_TRAINER_IMAGE_SUCCESS(HttpStatus.OK, "6005", "트레이너 정보 수정이 완료되었습니다."),
    TRAINER_SEARCH_SUCCESS(HttpStatus.OK, "6006", "트레이너 검색이 완료되었습니다."),

    // 7000: 게시물 성공 코드
    POST_CREATE_SUCCESS(HttpStatus.CREATED, "7001", "게시물 등록이 완료되었습니다."),
    POST_UPDATE_SUCCESS(HttpStatus.OK, "7002", "게시물 정보 수정이 완료되었습니다."),
    POST_EDIT_PHOTO_SUCCESS(HttpStatus.OK, "7004", "게시물 사진 수정 완료되었습니다."),
    GET_ALL_GENERAL_POST_SUCCESS(HttpStatus.OK, "7003", "등록된 모든 일반 게시물이 조회되었습니다."),
    GET_ALL_NOTICE_POST_SUCCESS(HttpStatus.OK, "7004", "등록된 모든 공지사항 게시물이 조회되었습니다."),
    GET_ONLY_POST_SUCCESS(HttpStatus.OK, "7005", "등록된 게시물 조회가 되었습니다."),
    POST_SEARCH_SUCCESS(HttpStatus.OK, "7006", "게시물 검색이 완료되었습니다."),

    // 8000 FitnessGoal 성공코드
    FITNESSGOAL_CREATE_SUCCESS(HttpStatus.CREATED, "8001", "운동 목표 생성이 완료되었습니다."),
    FITNESSGOAL_LIST_SUCCESS(HttpStatus.OK, "8002", "운동 목표 목록 조회가 완료되었습니다."),
    FITNESSGOAL_GET_SUCCESS(HttpStatus.OK, "8003", "운동 목표 조회가 완료되었습니다."),
    FITNESSGOAL_UPDATE_SUCCESS(HttpStatus.OK, "8004", "운동 목표 수정이 완료되었습니다."),
    FITNESSGOAL_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "8005", "운동 목표 삭제가 완료되었습니다."),
    FITNESSGOAL_CANCEL_SUCCESS(HttpStatus.OK, "8006", "운동 목표 취소가 완료되었습니다."),
    FITNESSGOAL_WEIGHTRECORD_CREATE_SUCCESS(HttpStatus.CREATED, "8007", "체중 기록 생성이 완료되었습니다."),
    FITNESSGOAL_WEIGHTRECORD_LIST_SUCCESS(HttpStatus.OK, "8008", "체중 기록 목록 조회가 완료되었습니다."),
    FITNESSGOAL_WEIGHTRECORD_GET_SUCCESS(HttpStatus.OK, "8009", "체중 기록 상세 조회가 완료되었습니다."),
    FITNESSGOAL_WEIGHTRECORD_UPDATE_SUCCESS(HttpStatus.OK, "8010", "체중 기록 수정이 완료되었습니다."),
    FITNESSGOAL_WEIGHTRECORD_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "8011", "체중 기록 삭제가 완료되었습니다."),
    FITNESSGOAL_DAILYRECORD_CREATE_SUCCESS(HttpStatus.CREATED, "8012", "일일 기록 생성 완료되었습니다."),
    FITNESSGOAL_DAILYRECORD_LIST_SUCCESS(HttpStatus.OK, "8013", "특정 목표의 일일 기록 목록 조회가 완료되었습니다."),
    FITNESSGOAL_DAILYRECORD_GET_SUCCESS(HttpStatus.OK, "8014", "일일 기록 상세 조회가 완료되었습니다."),
    FITNESSGOAL_DAILYRECORD_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "8015", "일일 기록 삭제가 완료되었습니다."),

    // 9000 s3 성공 코드
    S3_UPLOAD_SUCCESS(HttpStatus.CREATED, "9001", "이미지 업로드가 완료되었습니다"),
    S3_DELETE_SUCCESS(HttpStatus.OK, "9002", "이미지 삭제가 완료되었습니다."),
    S3_PRESIGNED_URL_GENERATED(HttpStatus.OK, "9003", "Presigned URL 생성이 완료되었습니다."),

    // 10000 : 채팅 성공 코드
    GET_ALL_CHATTING(HttpStatus.OK, "10000", "채팅 내역이 조회되었습니다."),
    CREATE_CHATROOM(HttpStatus.OK, "10001", "채팅방이 생성되었습니다."),
    GET_ALL_CHATROOM(HttpStatus.OK, "10002", "채팅방 목록이 조회되었습니다."),

    // 11000 : 리뷰 성공 코드
    REVIEW_CREATE_SUCCESS(HttpStatus.CREATED, "11001", "리뷰 등록이 완료되었습니다."),
    REVIEW_UPDATE_SUCCESS(HttpStatus.OK, "11002", "리뷰 수정이 완료되었습니다."),
    REVIEW_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "11003", "리뷰 삭제가 완료되었습니다."),
    REVIEW_GET_SUCCESS(HttpStatus.OK, "11004", "리뷰 조회가 완료되었습니다."),

    // 12000 : 맴버십 성공 코드
    POST_MEMBERSHIP_SUCCESS(HttpStatus.CREATED, "12001", "이용권 등록이 완료되었습니다."),
    GET_MEMBERSHIP_SUCCESS(HttpStatus.OK, "12002", "이용권 조회가 완료되었습니다."),
    PATCH_MEMBERSHIP_SUCCESS(HttpStatus.OK, "12003", "이용권 수정이 완료되었습니다."),
    DELETE_MEMBERSHIP_SUCCESS(HttpStatus.OK, "12004", "이용권 삭제가 완료되었습니다."),
    PURCHASE_MEMBERSHIP_SUCCESS(HttpStatus.OK, "12005", "이용권 구매가 완료되었습니다."),
    GET_MY_MEMBERSHIP_SUCCESS(HttpStatus.OK, "12006", "구매 이력 조회가 완료되었습니다."),
    GET_ACTIVE_MEMBERSHIP_SUCCESS(HttpStatus.OK, "12007", "활성화 된 이용권 조회가 완료되었습니다."),
    START_MEMBERSHIP_SUCCESS(HttpStatus.OK, "12008", "이용권 사용 시작"),
    GET_NOT_STARTED_MEMBERSHIP_SUCCESS(HttpStatus.OK, "12009",
        "아직 시작하지 않은 이용권 목록 조회가 완료되었습니다."),

    // 13000: 좋아요
    LIKE_TOGGLE_SUCCESS(HttpStatus.OK, "13001", "좋아요 상태가 변경되었습니다."),

    // 14000: 인기 검색어 조회
    GET_POPULAR_KEYWORD_SUCCESS(HttpStatus.OK, "14001", "인기 검색어 조회가 완료되었습니다."),
    GET_CHATROOM(HttpStatus.OK, "14002", "인기 검색어 조회가 완료되었습니다."),

    // 15000: 결제 성공 코드
    PAYMENT_PREPARE_SUCCESS(HttpStatus.OK, "14001", "결제 준비가 완료되었습니다."),
    PAYMENT_CONFIRM_SUCCESS(HttpStatus.OK, "14002", "결제 승인이 완료되었습니다."),
    PAYMENT_FAIL_SUCCESS(HttpStatus.OK, "14003", "결제 실패 처리가 완료되었습니다."),
    PAYMENT_HISTORY_GET_SUCCESS(HttpStatus.OK, "14004", "결제 내역 조회가 완료되었습니다."),
    PAYMENT_STATUS_GET_SUCCESS(HttpStatus.OK, "14005", "결제 상태 조회가 완료되었습니다."),
    PAYMENT_CANCEL_SUCCESS(HttpStatus.OK, "14006", "결제 취소가 완료되었습니다.");

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
