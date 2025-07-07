package org.example.fitpass.domain.point.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.point.dto.response.PointBalanceResponseDto;
import org.example.fitpass.domain.point.dto.response.PointCashOutResponseDto;
import org.example.fitpass.domain.point.dto.response.PointResponseDto;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;
import org.example.fitpass.domain.point.repository.PointRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    // 포인트 충전
    @Transactional
    public PointBalanceResponseDto chargePoint(Long userId, int amount, String description ) {
        log.info("[POINT CHARGE] 포인트 충전 시작 - USER_ID: {}, AMOUNT: {}, DESCRIPTION: {}", 
                userId, amount, description);
                
        User user = userRepository.findByIdOrElseThrow(userId);
        
        log.info("[POINT CHARGE BALANCE] 충전 전 잔액 확인 - USER_ID: {}, CURRENT_BALANCE: {}", 
                userId, user.getPointBalance());
                
        // 현재 잔액에서 충전될 포인트 추가
        int newBalance = user.getPointBalance() + amount;

        // 포인트 이력 저장
        Point point = new Point(user, amount, description, newBalance, PointType.CHARGE);
        pointRepository.save(point);

        // 사용자 잔액 업데이트
        user.updatePointBalance(newBalance);

        log.info("[POINT CHARGE SUCCESS] 포인트 충전 완료 - USER_ID: {}, CHARGED: {}, NEW_BALANCE: {}", 
                userId, amount, newBalance);

        return new PointBalanceResponseDto(newBalance);
    }

    // 포인트 사용
    @Transactional
    public PointBalanceResponseDto usePoint (Long userId, int amount, String description) {
        log.info("[POINT USE] 포인트 사용 시작 - USER_ID: {}, AMOUNT: {}, DESCRIPTION: {}", 
                userId, amount, description);
                
        User user = userRepository.findByIdOrElseThrow(userId);
        
        log.info("[POINT USE BALANCE] 사용 전 잔액 확인 - USER_ID: {}, CURRENT_BALANCE: {}, REQUEST_AMOUNT: {}", 
                userId, user.getPointBalance(), amount);
                
        // 잔액 부족 검증
        if (user.getPointBalance() < amount) {
            log.warn("[POINT USE FAILED] 잔액 부족 - USER_ID: {}, CURRENT_BALANCE: {}, REQUEST_AMOUNT: {}", 
                    userId, user.getPointBalance(), amount);
            throw new BaseException(ExceptionCode.INSUFFICIENT_POINT_BALANCE);
        }
        
        // 현재 잔액에서 사용한 포인트 차감
        int newBalance = user.getPointBalance() - amount;
        // 포인트 이력 저장
        Point point = new Point(user, amount, description, newBalance, PointType.USE);
        pointRepository.save(point);
        // 사용자 잔액 업데이트
        user.updatePointBalance(newBalance);

        log.info("[POINT USE SUCCESS] 포인트 사용 완료 - USER_ID: {}, USED: {}, NEW_BALANCE: {}", 
                userId, amount, newBalance);

        return new PointBalanceResponseDto(newBalance);
    }

    // 포인트 100% 환불
    @Transactional
    public PointBalanceResponseDto refundPoint (Long userId, int amount, String description) {
        log.info("[POINT REFUND] 포인트 환불 시작 - USER_ID: {}, AMOUNT: {}, DESCRIPTION: {}", 
                userId, amount, description);
                
        User user = userRepository.findByIdOrElseThrow(userId);

        log.info("[POINT REFUND BALANCE] 환불 전 잔액 확인 - USER_ID: {}, CURRENT_BALANCE: {}", 
                userId, user.getPointBalance());

        int newBalance = user.getPointBalance() + amount;

        Point point = new Point(user, amount, description, newBalance, PointType.REFUND);
        pointRepository.save(point);
        // 사용자 잔액 업데이트
        user.updatePointBalance(newBalance);

        log.info("[POINT REFUND SUCCESS] 포인트 환불 완료 - USER_ID: {}, REFUNDED: {}, NEW_BALANCE: {}", 
                userId, amount, newBalance);

        return new PointBalanceResponseDto(newBalance);
    }

    // 사용자 현금화용 - 90% 환불
    @Transactional
    public PointCashOutResponseDto cashOutPoint (Long userId, int amount, String description) {
        log.info("[POINT CASHOUT] 포인트 현금화 시작 - USER_ID: {}, AMOUNT: {}, DESCRIPTION: {}", 
                userId, amount, description);
                
        User user = userRepository.findByIdOrElseThrow(userId);

        log.info("[POINT CASHOUT BALANCE] 현금화 전 잔액 확인 - USER_ID: {}, CURRENT_BALANCE: {}, REQUEST_AMOUNT: {}", 
                userId, user.getPointBalance(), amount);

        // 잔액 부족 검증
        if (user.getPointBalance() < amount) {
            log.warn("[POINT CASHOUT FAILED] 잔액 부족 - USER_ID: {}, CURRENT_BALANCE: {}, REQUEST_AMOUNT: {}", 
                    userId, user.getPointBalance(), amount);
            throw new BaseException(ExceptionCode.INSUFFICIENT_POINT_BALANCE);
        }

        int requestedAmount = amount;
        int cashAmount = (int) (requestedAmount * 0.9);
        int newBalance = user.getPointBalance() - requestedAmount;

        log.info("[POINT CASHOUT CALCULATION] 현금화 계산 - USER_ID: {}, REQUESTED: {}, CASH_AMOUNT: {}, FEE: {}", 
                userId, requestedAmount, cashAmount, (requestedAmount - cashAmount));

        String descriptions = description != null ?
            description : "포인트 현금화";
        Point point = new Point(user, requestedAmount, descriptions, newBalance, PointType.CASH_OUT);
        pointRepository.save(point);

        user.updatePointBalance(newBalance);

        log.info("[POINT CASHOUT SUCCESS] 포인트 현금화 완료 - USER_ID: {}, REQUESTED: {}, CASH_AMOUNT: {}, NEW_BALANCE: {}", 
                userId, requestedAmount, cashAmount, newBalance);

        return new PointCashOutResponseDto(
            requestedAmount,
            cashAmount,
            newBalance
        );
    }

    // 포인트 잔액 조회
    @Transactional
    public PointBalanceResponseDto getPointBalance(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        return new PointBalanceResponseDto(user.getPointBalance());
    }

    // 포인트 이력 조회
    @Transactional(readOnly = true)
    public List<PointResponseDto> getPointHistory(Long userId) {
        List<Point> points = pointRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return points.stream()
            .map(PointResponseDto::from)
            .collect(Collectors.toList());
    }
}
