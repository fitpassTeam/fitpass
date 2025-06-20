package org.example.fitpass.domain.membership.service;

import static org.example.fitpass.common.error.ExceptionCode.ALREADY_STARTED;
import static org.example.fitpass.common.error.ExceptionCode.INVALID_GYM_MEMBERSHIP;
import static org.example.fitpass.common.error.ExceptionCode.INVALID_TOKEN;
import static org.example.fitpass.common.error.ExceptionCode.MEMBERSHIP_NOT_ACTIVE;
import static org.example.fitpass.common.error.ExceptionCode.NOT_FOUND_PURCHASE;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.membership.dto.response.MembershipPurchaseResponseDto;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;
import org.example.fitpass.domain.membership.repository.MembershipPurchaseRepository;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipPurchaseService {
    private final MembershipRepository membershipRepository;
    private final MembershipPurchaseRepository purchaseRepository;
    private final UserRepository userRepository;

    @Transactional
    public MembershipPurchaseResponseDto purchase(Long membershipId, Long userId, Long gymId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Membership membership = membershipRepository.findByIdOrElseThrow(membershipId);

        if (!membership.getGym().getId().equals(gymId)) {
            throw new BaseException(INVALID_GYM_MEMBERSHIP);
        }


        Gym gym = membership.getGym();
        LocalDateTime now = LocalDateTime.now();


        MembershipPurchase purchase = new MembershipPurchase(membership, gym, user, now);
        purchaseRepository.save(purchase);
        return MembershipPurchaseResponseDto.from(purchase);
    }

    @Transactional
    public MembershipPurchaseResponseDto startMembership(Long purchaseId, Long userId){
        MembershipPurchase purchase = purchaseRepository.findByIdOrElseThrow(purchaseId);

        if (!purchase.getUser().getId().equals(userId)) {
            throw new BaseException(INVALID_TOKEN);
        }

        if (!purchase.isNotStarted()) {
            throw new BaseException(ALREADY_STARTED);
        }

        purchase.activate(LocalDateTime.now());
        return MembershipPurchaseResponseDto.from(purchase);
    }

    @Transactional
    public List<MembershipPurchaseResponseDto> getNotStartedMemberships(Long userId){
        User user = userRepository.findByIdOrElseThrow(userId);
        List<MembershipPurchase> purchases = purchaseRepository.findAllNotStartedByUser(user);
        return purchases.stream()
            .map(MembershipPurchaseResponseDto::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MembershipPurchaseResponseDto> getMyPurchases(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        return purchaseRepository.findAllByUser(user).stream()
            .map(MembershipPurchaseResponseDto::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public MembershipPurchaseResponseDto getMyActive(Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        List<MembershipPurchase> activeList = purchaseRepository.findAllActiveByUser(user, LocalDateTime.now());

        MembershipPurchase active = activeList.stream()
            .findFirst()
            .orElseThrow(() -> new BaseException(MEMBERSHIP_NOT_ACTIVE));

        return MembershipPurchaseResponseDto.from(active);
    }
}
