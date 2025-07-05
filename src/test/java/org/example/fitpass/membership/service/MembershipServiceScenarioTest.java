package org.example.fitpass.membership.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.membership.dto.response.MembershipPurchaseResponseDto;
import org.example.fitpass.domain.membership.dto.response.MembershipResponseDto;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;
import org.example.fitpass.domain.membership.repository.MembershipPurchaseRepository;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.example.fitpass.domain.membership.service.MembershipPurchaseService;
import org.example.fitpass.domain.membership.service.MembershipService;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;
import org.example.fitpass.domain.point.repository.PointRepository;
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
@DisplayName("MembershipService 시나리오 테스트")
class MembershipServiceScenarioTest {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private MembershipPurchaseService membershipPurchaseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MembershipPurchaseRepository purchaseRepository;

    @Autowired
    private PointRepository pointRepository;

    private User owner1, owner2, user1, user2;
    private Gym gym1, gym2;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        purchaseRepository.deleteAll();
        membershipRepository.deleteAll();
        pointRepository.deleteAll();
        gymRepository.deleteAll();
        userRepository.deleteAll();

        // 체육관 소유자들 생성
        owner1 = userRepository.save(new User(
            "owner1@test.com", null, "password123", "박사장",
            "010-1111-1111", 35, "서울시 강남구",
            Gender.MAN, UserRole.OWNER, "LOCAL"
        ));

        owner2 = userRepository.save(new User(
            "owner2@test.com", null, "password123", "최사장",
            "010-2222-2222", 32, "서울시 마포구",
            Gender.WOMAN, UserRole.OWNER, "LOCAL"
        ));

        // 일반 사용자들 생성
        user1 = userRepository.save(new User(
            "user1@test.com", null, "password123", "김회원",
            "010-3333-3333", 25, "서울시 강남구",
            Gender.MAN, UserRole.USER, "LOCAL"
        ));

        user2 = userRepository.save(new User(
            "user2@test.com", null, "password123", "이회원",
            "010-4444-4444", 28, "서울시 서초구",
            Gender.WOMAN, UserRole.USER, "LOCAL"
        ));

        // 체육관들 생성
        gym1 = gymRepository.save(Gym.of(
            List.of("gym1_1.jpg"),
            "강남 피트니스", "02-1111-1111", "강남 최고의 헬스장",
            "서울시", "강남구", "테헤란로 100",
            LocalTime.of(6, 0), LocalTime.of(23, 59),
            "24시간 운영하는 프리미엄 헬스장", owner1
        ));

        gym2 = gymRepository.save(Gym.of(
            List.of("gym2_1.jpg"),
            "마포 스포츠센터", "02-2222-2222", "가족 친화적인 스포츠센터",
            "서울시", "마포구", "월드컵로 200",
            LocalTime.of(5, 30), LocalTime.of(23, 30),
            "수영장과 헬스장을 갖춘 종합 스포츠센터", owner2
        ));

        // 사용자들에게 포인트 지급 및 잔액 업데이트
        pointRepository.save(new Point(user1, 500000, "시나리오 테스트 포인트", 500000, PointType.CHARGE));
        user1.updatePointBalance(500000); // 추가: 사용자 잔액 업데이트
        user1 = userRepository.save(user1); // 추가: 업데이트된 사용자 저장

        pointRepository.save(new Point(user2, 300000, "시나리오 테스트 포인트", 300000, PointType.CHARGE));
        user2.updatePointBalance(300000); // 추가: 사용자 잔액 업데이트
        user2 = userRepository.save(user2); // 추가: 업데이트된 사용자 저장
    }

    @Test
    @DisplayName("시나리오 1: 체육관 사업자의 이용권 전략 수립부터 판매까지")
    void scenario_GymBusinessStrategy() {
        // 1단계: 체육관 사업자가 시장 조사 후 다양한 이용권 기획
        MembershipResponseDto basicPlan = membershipService.createMembership(
            gym1.getId(), owner1.getId(), "기본 1개월권", 70000, "헬스장 기본 이용", 30
        );

        MembershipResponseDto standardPlan = membershipService.createMembership(
            gym1.getId(), owner1.getId(), "스탠다드 3개월권", 180000, "헬스장 + 그룹 PT", 90
        );

        // 2단계: 이용권 목록 확인 및 가격 정책 검토
        List<MembershipResponseDto> memberships = membershipService.getAllByGym(gym1.getId());
        assertThat(memberships).hasSize(2);

        // 3단계: 시장 반응에 따른 가격 조정
        membershipService.updateMembership(
            gym1.getId(), standardPlan.id(), owner1.getId(),
            "스탠다드 3개월권 특가", 160000, "헬스장 + 그룹 PT + 영양상담", 90
        );

        // 4단계: 사용자들의 구매 반응 확인
        membershipPurchaseService.purchase(basicPlan.id(), user1.getId(), gym1.getId(), LocalDate.now().plusDays(1));
        membershipPurchaseService.purchase(standardPlan.id(), user2.getId(), gym1.getId(), LocalDate.now().plusDays(2));

        // 5단계: 판매 데이터 분석
        List<MembershipPurchase> sales = purchaseRepository.findAllByGym(gym1);
        assertThat(sales).hasSize(2);
    }

    @Test
    @DisplayName("시나리오 2: 사용자의 체육관 탐색부터 이용권 구매까지")
    void scenario_UserGymExplorationToPurchase() {
        // 1단계: 두 체육관에서 각각 이용권 출시
        MembershipResponseDto gym1Basic = membershipService.createMembership(
            gym1.getId(), owner1.getId(), "강남점 기본권", 80000, "강남점 헬스장 이용", 30
        );

        MembershipResponseDto gym2Basic = membershipService.createMembership(
            gym2.getId(), owner2.getId(), "마포점 기본권", 75000, "마포점 수영장+헬스장", 30
        );

        // 2단계: 사용자가 두 체육관의 이용권 비교 검토
        List<MembershipResponseDto> gym1Options = membershipService.getAllByGym(gym1.getId());
        List<MembershipResponseDto> gym2Options = membershipService.getAllByGym(gym2.getId());

        assertThat(gym1Options.get(0).price()).isEqualTo(80000);
        assertThat(gym2Options.get(0).price()).isEqualTo(75000);

        // 3단계: 가격과 혜택을 고려한 선택
        MembershipPurchaseResponseDto purchase = membershipPurchaseService.purchase(
            gym2Basic.id(), user1.getId(), gym2.getId(), LocalDate.now().plusDays(1)
        );

        assertThat(purchase.membershipName()).isEqualTo("마포점 기본권");
        assertThat(purchase.price()).isEqualTo(75000);
    }
}
