package org.example.fitpass.domain.membership.service;

import static org.example.fitpass.common.error.ExceptionCode.INVALID_GYM_MEMBERSHIP;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final MembershipRepository membershipRepository;

    // 이용권 생성
    @Transactional
    public MembershipResponseDto createMembership(Long gymId, Long userId, String name, int price, String content, int durationInDays) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 이 체육관의 소유자인지 확인
        if (!Objects.equals(gym.getOwner().getId(), userId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }

        Membership membership = Membership.of( name,price,content,durationInDays);
        membership.assignToGym(gym);
        membershipRepository.save(membership);
        return MembershipResponseDto.fromEntity(
            membership.getId(),
            membership.getName(),
            membership.getPrice(),
            membership.getContent(),
            membership.getDurationInDays());
    }
    // 모든 이용권 조회
    @Transactional(readOnly = true)
    public List<MembershipResponseDto> getAllByGym(Long gymId, Long userId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        return membershipRepository.findAllByGym(gym).stream()
            .map(MembershipResponseDto::of)
            .toList();
    }
    // 이용권 상세 조회
    @Transactional(readOnly = true)
    public MembershipResponseDto getMembershipById(Long gymId, Long membershipId, Long userId) {
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

        return MembershipResponseDto.fromEntity(
            membership.getId(),
            membership.getName(),
            membership.getPrice(),
            membership.getContent(),
            membership.getDurationInDays());
    }
    // 이용권 수정
    @Transactional
    public MembershipResponseDto updateMembership(Long gymId, Long membershipId, Long userId, String name, int price,
        String content, int durationInDays) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 이 체육관의 소유자인지 확인
        if (!Objects.equals(gym.getOwner().getId(), userId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 이용권 조회
        Membership membership = membershipRepository.findByIdOrElseThrow(membershipId);
        // 체육관의 이용권인지 확인하는 로직
        if (!membership.getGym().getId().equals(gymId)) {
            throw new BaseException(INVALID_GYM_MEMBERSHIP);
        }
        // 이용권 수정
        membership.update(name, price, content, durationInDays);
        return MembershipResponseDto.fromEntity(
            membership.getId(),
            membership.getName(),
            membership.getPrice(),
            membership.getContent(),
            membership.getDurationInDays()
        );
    }
    // 이용권 삭제
    @Transactional
    public void deleteMembership(Long gymId, Long membershipId, Long userId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 이 체육관의 소유자인지 확인
        if (!Objects.equals(gym.getOwner().getId(), userId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 이용권 조회
        Membership membership = membershipRepository.findByIdOrElseThrow(membershipId);
        // 체육관의 이용권인지 확인하는 로직
        if (!membership.getGym().getId().equals(gymId)) {
            throw new BaseException(INVALID_GYM_MEMBERSHIP);
        }
        // 이용권 삭제
        membershipRepository.delete(membership);
    }
}
