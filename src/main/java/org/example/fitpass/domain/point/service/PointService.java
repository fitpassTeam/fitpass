package org.example.fitpass.domain.point.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.point.dto.request.PointCashOutRequestDto;
import org.example.fitpass.domain.point.dto.request.PointChargeRequestDto;
import org.example.fitpass.domain.point.dto.request.PointUseRefundRequestDto;
import org.example.fitpass.domain.point.dto.response.PointCashOutResponseDto;
import org.example.fitpass.domain.point.dto.response.PointResponseDto;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;
import org.example.fitpass.domain.point.repository.PointRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    // 포인트 충전
    @Transactional
    public int chargePoint(Long userId, PointChargeRequestDto pointChargeRequestDto, String description ) {
        User user = userRepository.findByIdOrElseThrow(userId);
        // 현재 잔액에서 충전될 포인트 추가
        int newBalance = user.getPointBalance() + pointChargeRequestDto.getAmount();

        // 포인트 이력 저장
        Point point = new Point(user, pointChargeRequestDto.getAmount(), description, newBalance, PointType.CHARGE);
        pointRepository.save(point);

        // 사용자 잔액 업데이트
        user.updatePointBalance(newBalance);

        return newBalance;
    }

    // 포인트 사용
    @Transactional
    public int usePoint (Long userId, PointUseRefundRequestDto pointUseRefundRequestDto) {
        User user = userRepository.findByIdOrElseThrow(userId);
        // 잔액 부족 검증
        if (user.getPointBalance() < pointUseRefundRequestDto.getAmount()) {
            throw new BaseException(ExceptionCode.INSUFFICIENT_POINT_BALANCE);
        }
        // 현재 잔액에서 사용한 포인트 차감
        int newBalance = user.getPointBalance() - pointUseRefundRequestDto.getAmount();
        // 포인트 이력 저장
        Point point = new Point(user, pointUseRefundRequestDto.getAmount(), pointUseRefundRequestDto.getDescription(), newBalance, PointType.USE);
        pointRepository.save(point);
        // 사용자 잔액 업데이트
        user.updatePointBalance(newBalance);

        return newBalance;
    }

    // 포인트 100% 환불
    @Transactional
    public int refundPoint (Long userId, PointUseRefundRequestDto pointUseRefundRequestDto) {
        User user = userRepository.findByIdOrElseThrow(userId);

        int newBalance = user.getPointBalance() + pointUseRefundRequestDto.getAmount();

        Point point = new Point(user, pointUseRefundRequestDto.getAmount(), pointUseRefundRequestDto.getDescription(), newBalance, PointType.REFUND);
        pointRepository.save(point);
        // 사용자 잔액 업데이트
        user.updatePointBalance(newBalance);

        return newBalance;
    }

    // 사용자 현금화용 - 90% 환불
    @Transactional
    public PointCashOutResponseDto cashOutPoint (Long userId, PointCashOutRequestDto pointCashOutRequestDto) {
        User user = userRepository.findByIdOrElseThrow(userId);

        // 잔액 부족 검증
        if (user.getPointBalance() < pointCashOutRequestDto.getAmount()) {
            throw new BaseException(ExceptionCode.INSUFFICIENT_POINT_BALANCE);
        }

        int requestedAmount = pointCashOutRequestDto.getAmount();
        int cashAmount = (int)  (requestedAmount * 0.9);
        int newBalance = user.getPointBalance() - requestedAmount;

        String description = pointCashOutRequestDto.getDescription() != null ?
            pointCashOutRequestDto.getDescription() : "포인트 현금화";
        Point point = new Point(user, requestedAmount, description, newBalance, PointType.CASH_OUT);
        pointRepository.save(point);

        user.updatePointBalance(newBalance);

        return new PointCashOutResponseDto(
            requestedAmount,
            cashAmount,
            newBalance
        );
    }

    // 포인트 잔액 조회
    @Transactional
    public int getPointBalance(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        return user.getPointBalance();
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
