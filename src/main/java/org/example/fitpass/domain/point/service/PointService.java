package org.example.fitpass.domain.point.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.point.dto.PointChargeRequestDto;
import org.example.fitpass.domain.point.dto.PointResponseDto;
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
    public void chargePoint(Long userId, PointChargeRequestDto pointChargeRequestDto, String description ) {
        User user = userRepository.findByIdOrElseThrow(userId);

        int newBalance = user.getPointBalance() + pointChargeRequestDto.getAmount();

        // 포인트 이력 저장
        Point point = new Point(user, pointChargeRequestDto.getAmount(), description, newBalance, PointType.CHARGE);
        pointRepository.save(point);

        // 사용자 잔액 업데이트
        user.updatePointBalance(newBalance);
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
