package org.example.fitpass.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.example.fitpass.domain.membership.service.MembershipService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("MembershipService 통합 테스트")
class MembershipServiceIntegrationTest {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private User savedOwner;
    private User savedUser;
    private Gym savedGym;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        membershipRepository.deleteAll();
        gymRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 데이터 설정
        savedOwner = userRepository.save(new User(
            "owner@test.com",
            null,
            "password123",
            "체육관사장",
            "010-1234-5678",
            35,
            "서울시 강남구",
            Gender.MAN,
            UserRole.OWNER,
            "LOCAL"
        ));

        savedUser = userRepository.save(new User(
            "user@test.com",
            null,
            "password123",
            "일반사용자",
            "010-9876-5432",
            25,
            "서울시 서초구",
            Gender.WOMAN,
            UserRole.USER,
            "LOCAL"
        ));

        savedGym = gymRepository.save(Gym.of(
            List.of("gym1.jpg", "gym2.jpg"),
            "테스트 헬스장",
            "02-1234-5678",
            "최고의 헬스장입니다",
            "서울시",
            "강남구",
            "테헤란로 332",
            LocalTime.of(6, 0),
            LocalTime.of(23, 59),
            "깨끗하고 시설이 좋은 헬스장",
            savedOwner
        ));
    }

    @Test
    @DisplayName("이용권 생성 통합 테스트 - 성공")
    void createMembership_Integration_Success() {
        // when
        MembershipResponseDto result = membershipService.createMembership(
            savedGym.getId(), savedOwner.getId(), "1개월 자유 이용권", 80000, "헬스장 자유 이용", 30
        );

        // then
        assertThat(result.name()).isEqualTo("1개월 자유 이용권");
        assertThat(result.price()).isEqualTo(80000);
        assertThat(result.content()).isEqualTo("헬스장 자유 이용");
        assertThat(result.durationInDays()).isEqualTo(30);

        // 데이터베이스에 저장되었는지 확인
        List<Membership> memberships = membershipRepository.findAllByGym(savedGym);
        assertThat(memberships).hasSize(1);
        assertThat(memberships.get(0).getName()).isEqualTo("1개월 자유 이용권");
    }

    @Test
    @DisplayName("이용권 생성 통합 테스트 - 일반 사용자 권한 없음")
    void createMembership_Integration_NotGymOwner() {
        // when & then
        assertThatThrownBy(() -> membershipService.createMembership(
            savedGym.getId(), savedUser.getId(), "1개월 자유 이용권", 80000, "헬스장 자유 이용", 30
        )).isInstanceOf(BaseException.class)
          .hasMessageContaining(ExceptionCode.NOT_GYM_OWNER.getMessage());

        // 데이터베이스에 저장되지 않았는지 확인
        List<Membership> memberships = membershipRepository.findAllByGym(savedGym);
        assertThat(memberships).isEmpty();
    }

    @Test
    @DisplayName("체육관 이용권 목록 조회 통합 테스트")
    void getAllByGym_Integration_Success() {
        // given - 여러 이용권 생성
        membershipService.createMembership(
            savedGym.getId(), savedOwner.getId(), "1개월 기본권", 80000, "기본 이용", 30
        );
        membershipService.createMembership(
            savedGym.getId(), savedOwner.getId(), "3개월 프리미엄권", 200000, "프리미엄 이용", 90
        );

        // when
        List<MembershipResponseDto> result = membershipService.getAllByGym(savedGym.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(MembershipResponseDto::name))
            .containsExactlyInAnyOrder("1개월 기본권", "3개월 프리미엄권");
    }

    @Test
    @DisplayName("이용권 상세 조회 통합 테스트")
    void getMembershipById_Integration_Success() {
        // given
        MembershipResponseDto created = membershipService.createMembership(
            savedGym.getId(), savedOwner.getId(), "1개월 자유 이용권", 80000, "헬스장 자유 이용", 30
        );

        // when
        MembershipResponseDto result = membershipService.getMembershipById(savedGym.getId(), created.id());

        // then
        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.name()).isEqualTo("1개월 자유 이용권");
        assertThat(result.price()).isEqualTo(80000);
    }

    @Test
    @DisplayName("이용권 수정 통합 테스트")
    void updateMembership_Integration_Success() {
        // given
        MembershipResponseDto created = membershipService.createMembership(
            savedGym.getId(), savedOwner.getId(), "1개월 자유 이용권", 80000, "헬스장 자유 이용", 30
        );

        // when
        MembershipResponseDto result = membershipService.updateMembership(
            savedGym.getId(), created.id(), savedOwner.getId(),
            "수정된 이용권", 90000, "수정된 내용", 45
        );

        // then
        assertThat(result.name()).isEqualTo("수정된 이용권");
        assertThat(result.price()).isEqualTo(90000);
        assertThat(result.content()).isEqualTo("수정된 내용");
        assertThat(result.durationInDays()).isEqualTo(45);

        // 데이터베이스에서 실제로 수정되었는지 확인
        Membership updated = membershipRepository.findById(created.id()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정된 이용권");
        assertThat(updated.getPrice()).isEqualTo(90000);
    }

    @Test
    @DisplayName("이용권 삭제 통합 테스트")
    void deleteMembership_Integration_Success() {
        // given
        MembershipResponseDto created = membershipService.createMembership(
            savedGym.getId(), savedOwner.getId(), "1개월 자유 이용권", 80000, "헬스장 자유 이용", 30
        );

        // when
        membershipService.deleteMembership(savedGym.getId(), created.id(), savedOwner.getId());

        // then
        assertThat(membershipRepository.findById(created.id())).isEmpty();
        List<Membership> memberships = membershipRepository.findAllByGym(savedGym);
        assertThat(memberships).isEmpty();
    }

    @Test
    @DisplayName("빈 이용권 목록 조회")
    void getAllByGym_Integration_EmptyList() {
        // when
        List<MembershipResponseDto> result = membershipService.getAllByGym(savedGym.getId());

        // then
        assertThat(result).isEmpty();
    }
}
