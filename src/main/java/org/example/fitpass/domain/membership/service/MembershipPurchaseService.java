package org.example.fitpass.domain.membership.service;

import static org.example.fitpass.common.error.ExceptionCode.INVALID_GYM_MEMBERSHIP;
import static org.example.fitpass.common.error.ExceptionCode.MEMBERSHIP_NOT_ACTIVE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.membership.dto.response.MembershipPurchaseResponseDto;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;
import org.example.fitpass.domain.membership.repository.MembershipPurchaseRepository;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipPurchaseService {
    private final MembershipRepository membershipRepository;
    private final MembershipPurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final PointService pointService;

    // 이용권 구매
    @Transactional
    public MembershipPurchaseResponseDto purchase(Long membershipId, Long userId, Long gymId, LocalDate activationDate) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 이용권 조회
        Membership membership = membershipRepository.findByIdOrElseThrow(membershipId);
        // 체육관의 이용권인지 확인하는 로직
        if (!membership.getGym().getId().equals(gymId)) {
            throw new BaseException(INVALID_GYM_MEMBERSHIP);
        }

        // 활성화 날짜 검증 (구매일 기준 7일 이내)
        LocalDate today = LocalDate.now();
        if (activationDate.isBefore(today)) {
            throw new BaseException(ExceptionCode.INVALID_ACTIVATION_DATE_PAST);
        }
        if (activationDate.isAfter(today.plusDays(7))) {
            throw new BaseException(ExceptionCode.INVALID_ACTIVATION_DATE_TOO_FAR);
        }

        // 포인트 차감
        pointService.usePoint(userId, membership.getPrice(), "이용권 구매 - " + membership.getName());

        // 구매 시점 기록
        LocalDateTime purchaseTime = LocalDateTime.now();
        // 예약 활성화 날짜 설정
        LocalDateTime scheduledStartTime = activationDate.atStartOfDay();

        // 이용권 생성 (예약 활성화)
        MembershipPurchase purchase = new MembershipPurchase(membership, gym, user, purchaseTime, scheduledStartTime);
        purchaseRepository.save(purchase);

        return MembershipPurchaseResponseDto.from(purchase);
    }
    // 미활성화 이용권 조회
    @Transactional(readOnly = true)
    public List<MembershipPurchaseResponseDto> getNotStartedMemberships(Long userId){
        User user = userRepository.findByIdOrElseThrow(userId);
        List<MembershipPurchase> purchases = purchaseRepository.findAllNotStartedByUser(user);
        return purchases.stream()
            .map(MembershipPurchaseResponseDto::from)
            .toList();
    }
    // 모든 구매 이력 조회
    @Transactional(readOnly = true)
    public List<MembershipPurchaseResponseDto> getMyPurchases(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        return purchaseRepository.findAllByUser(user).stream()
            .map(MembershipPurchaseResponseDto::from)
            .toList();
    }
    // 현재 활성 이용권 조회
    @Transactional(readOnly = true)
    public MembershipPurchaseResponseDto getMyActive(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        List<MembershipPurchase> activeList = purchaseRepository.findAllActiveByUser(user, LocalDateTime.now());

        // 다중 활성 이용권에 예외처리
        if (activeList.isEmpty()) {
            throw new BaseException(MEMBERSHIP_NOT_ACTIVE);
        }

        if (activeList.size() > 1) {
            // 여러 개가 있으면 가장 최근 활성화된 것
            MembershipPurchase active = activeList.stream()
                .max(Comparator.comparing(MembershipPurchase::getStartDate))  // 가장 최근 시작
                .orElseThrow(() -> new BaseException(MEMBERSHIP_NOT_ACTIVE));
            return MembershipPurchaseResponseDto.from(active);
        }

        // 하나만 있으면 그것을 반환
        return MembershipPurchaseResponseDto.from(activeList.get(0));
    }
}
