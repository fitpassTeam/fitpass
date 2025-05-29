package org.example.fitpass.domain.point.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.point.dto.request.PointChargeRequestDto;
import org.example.fitpass.domain.point.dto.request.PointUseRequestDto;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;
import org.example.fitpass.domain.point.repository.PointRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    // 포인트 충전
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
    public int usePoint (Long userId, PointUseRequestDto  pointUseRequestDto) {
        User user = userRepository.findByIdOrElseThrow(userId);
        // 잔액 부족 검증
        if (user.getPointBalance() < pointUseRequestDto.getAmount()) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다.");
        }
        // 현재 잔액에서 사용한 포인트 차감
        int newBalance = user.getPointBalance() - pointUseRequestDto.getAmount();
        // 포인트 이력 저장
        Point point = new Point(user, pointUseRequestDto.getAmount(), pointUseRequestDto.getDescription(), newBalance, PointType.USE);
        pointRepository.save(point);
        // 사용자 잔액 업데이트
        user.updatePointBalance(newBalance);

        return newBalance;
    }

    // 포인트 잔액 조회
    public int getPointBalance(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        return user.getPointBalance();
    }

    // 포인트 이력 조회
    public List<Point> getPointHistory(Long userId) {
        return pointRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
