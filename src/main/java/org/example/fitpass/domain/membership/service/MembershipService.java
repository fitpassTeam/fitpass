package org.example.fitpass.domain.membership.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.membership.dto.request.MembershipRequestDto;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final GymRepository gymRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public MembershipResponseDto createMembership(Long gymId, String name, int price, String content) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Membership membership = Membership.of( name,price,content);
        membership.assignToGym(gym);
        membershipRepository.save(membership);
        return MembershipResponseDto.fromEntity(membership);
    }

    @Transactional(readOnly = true)
    public List<MembershipResponseDto> getAllByGym(Long gymId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        return membershipRepository.findAllByGym(gym).stream()
            .map(MembershipResponseDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public MembershipResponseDto getById(Long gymId, Long id) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Membership membership = membershipRepository.findByIdOrElseThrow(id);
        membership.validateBelongsToGym(gym);
        return MembershipResponseDto.fromEntity(membership);
    }

    @Transactional
    public MembershipResponseDto updateMembership(Long gymId, Long id, String name, int price,
        String content) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Membership membership = membershipRepository.findByIdOrElseThrow(id);
        membership.validateBelongsToGym(gym);
        membership.update(name, price, content);
        return MembershipResponseDto.of(
            membership.getName(),
            membership.getPrice(),
            membership.getContent()
        );
    }

    @Transactional
    public void deleteMembership(Long gymId, Long id) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Membership membership = membershipRepository.findByIdOrElseThrow(id);
        membership.validateBelongsToGym(gym);
        membershipRepository.delete(membership);
    }
}
