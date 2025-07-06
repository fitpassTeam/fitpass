package org.example.fitpass.membership.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.config.RedisService;
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
import org.example.fitpass.domain.notify.entity.Notify;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("MembershipService 시나리오 테스트")
class MembershipScenarioTest {

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

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> customStringRedisTemplate;

    @MockBean
    @Qualifier("notifyRedisTemplate")
    private RedisTemplate<String, List<Notify>> notifyRedisTemplate;

    private User user1, user2, owner1, owner2;
    private Gym gym1, gym2;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        purchaseRepository.deleteAll();
        membershipRepository.deleteAll();
        pointRepository.deleteAll();
        gymRepository.deleteAll();
        userRepository.deleteAll();

        // 사용자들 생성
        user1 = userRepository.save(new User(
            "user1@test.com", null, "password123", "김회원",
            "010-1111-1111", 25, "서울시 강남구",
            Gender.MAN, UserRole.USER, "LOCAL"
        ));

        user2 = userRepository.save(new User(
            "user2@test.com", null, "password123", "이회원",
            "010-2222-2222", 28, "서울시 서초구",
            Gender.WOMAN, UserRole.USER, "LOCAL"
        ));

        // 체육관 소유자들 생성
        owner1 = userRepository.save(new User(
            "owner1@test.com", null, "password123", "박사장",
            "010-3333-3333", 35, "서울시 강남구",
            Gender.MAN, UserRole.OWNER, "LOCAL"
        ));

        owner2 = userRepository.save(new User(
            "owner2@test.com", null, "password123", "최사장",
            "010-4444-4444", 32, "서울시 마포구",
            Gender.WOMAN, UserRole.OWNER, "LOCAL"
        ));

        // 체육관들 생성
        gym1 = gymRepository.save(Gym.of(
            List.of("gym1_1.jpg", "gym1_2.jpg"),
            "강남 피트니스", "02-1111-1111", "강남 최고의 헬스장",
            "서울시", "강남구", "테헤란로 100",
            LocalTime.of(6, 0), LocalTime.of(23, 59),
            "24시간 운영하는 프리미엄 헬스장", owner1
        ));

        gym2 = gymRepository.save(Gym.of(
            List.of("gym2_1.jpg", "gym2_2.jpg"),
            "마포 스포츠센터", "02-2222-2222", "가족 친화적인 스포츠센터",
            "서울시", "마포구", "월드컵로 200",
            LocalTime.of(5, 30), LocalTime.of(23, 30),
            "수영장과 헬스장을 갖춘 종합 스포츠센터", owner2
        ));

        // 사용자들에게 충분한 포인트 지급 및 잔액 업데이트
        pointRepository.save(new Point(user1, 500000, "시나리오 테스트 포인트", 500000, PointType.CHARGE));
        user1.updatePointBalance(500000); // 사용자 잔액 업데이트
        user1 = userRepository.save(user1); // 업데이트된 사용자 저장
        
        pointRepository.save(new Point(user2, 300000, "시나리오 테스트 포인트", 300000, PointType.CHARGE));
        user2.updatePointBalance(300000); // 사용자 잔액 업데이트
        user2 = userRepository.save(user2); // 업데이트된 사용자 저장
    }

    @Test
    @DisplayName("시나리오 1: 체육관 소유자의 이용권 관리 전체 프로세스")
    void scenario_GymOwnerMembershipManagement() {
        // 1단계: 체육관 소유자가 다양한 이용권 등록
        MembershipResponseDto basicMembership = membershipService.createMembership(
            gym1.getId(), owner1.getId(), "1개월 기본권", 80000, "헬스장 자유 이용", 30
        );

        MembershipResponseDto premiumMembership = membershipService.createMembership(
            gym1.getId(), owner1.getId(), "3개월 프리미엄권", 200000, "헬스장 + PT 10회", 90
        );

        // 2단계: 등록된 이용권 목록 확인
        List<MembershipResponseDto> allMemberships = membershipService.getAllByGym(gym1.getId());
        assertThat(allMemberships).hasSize(2);
        assertThat(allMemberships.stream().map(MembershipResponseDto::name))
            .containsExactlyInAnyOrder("1개월 기본권", "3개월 프리미엄권");

        // 3단계: 이용권 정보 수정
        MembershipResponseDto updatedPremium = membershipService.updateMembership(
            gym1.getId(), premiumMembership.id(), owner1.getId(),
            "3개월 프리미엄권 플러스", 220000, "헬스장 + PT 15회 + 영양상담", 90
        );

        assertThat(updatedPremium.name()).isEqualTo("3개월 프리미엄권 플러스");
        assertThat(updatedPremium.price()).isEqualTo(220000);
        assertThat(updatedPremium.content()).contains("PT 15회");
    }

    @Test
    @DisplayName("시나리오 2: 사용자의 이용권 구매부터 활성화까지")
    void scenario_UserMembershipPurchaseToActivation() {
        // 1단계: 체육관에 이용권 등록
        MembershipResponseDto membership = membershipService.createMembership(
            gym1.getId(), owner1.getId(), "1개월 자유 이용권", 80000, "헬스장 자유 이용 가능", 30
        );

        // 2단계: 사용자가 이용권 구매 (7일 후 활성화 예약)
        LocalDate activationDate = LocalDate.now().plusDays(7);
        MembershipPurchaseResponseDto purchase = membershipPurchaseService.purchase(
            membership.id(), user1.getId(), gym1.getId(), activationDate
        );

        assertThat(purchase.membershipName()).isEqualTo("1개월 자유 이용권");
        assertThat(purchase.price()).isEqualTo(80000);
        assertThat(purchase.durationInDays()).isEqualTo(30);
        assertThat(purchase.isActive()).isFalse(); // 아직 활성화되지 않음

        // 3단계: 미활성화 이용권 목록에서 확인
        List<MembershipPurchaseResponseDto> notStarted = membershipPurchaseService.getNotStartedMemberships(user1.getId());
        assertThat(notStarted).hasSize(1);
        assertThat(notStarted.get(0).membershipName()).isEqualTo("1개월 자유 이용권");

        // 4단계: 예약된 날짜에 이용권 활성화 (과거 시간으로 설정하여 확실히 활성 상태 만들기)
        MembershipPurchase purchaseEntity = purchaseRepository.findById(purchase.id()).orElseThrow();
        LocalDateTime activationTime = LocalDateTime.now().minusMinutes(10); // 10분 전에 활성화
        purchaseEntity.activate(activationTime);
        purchaseRepository.save(purchaseEntity);

        // 5단계: 활성 이용권으로 조회 가능
        MembershipPurchaseResponseDto activeMembership = membershipPurchaseService.getMyActive(user1.getId());
        assertThat(activeMembership.isActive()).isTrue();
        assertThat(activeMembership.membershipName()).isEqualTo("1개월 자유 이용권");
    }

    @Test
    @DisplayName("시나리오 3: 다중 체육관 이용권 관리")
    void scenario_MultipleGymMembershipManagement() {
        // 1단계: 각 체육관에 이용권 등록
        MembershipResponseDto gym1Membership = membershipService.createMembership(
            gym1.getId(), owner1.getId(), "강남 1개월권", 90000, "강남점 자유 이용", 30
        );

        MembershipResponseDto gym2Membership = membershipService.createMembership(
            gym2.getId(), owner2.getId(), "마포 1개월권", 70000, "마포점 자유 이용", 30
        );

        // 2단계: 사용자가 두 체육관의 이용권 모두 구매
        MembershipPurchaseResponseDto purchase1 = membershipPurchaseService.purchase(
            gym1Membership.id(), user1.getId(), gym1.getId(), LocalDate.now().plusDays(1)
        );

        MembershipPurchaseResponseDto purchase2 = membershipPurchaseService.purchase(
            gym2Membership.id(), user1.getId(), gym2.getId(), LocalDate.now().plusDays(2)
        );

        // 3단계: 구매 이력 확인
        List<MembershipPurchaseResponseDto> purchases = membershipPurchaseService.getMyPurchases(user1.getId());
        assertThat(purchases).hasSize(2);
        assertThat(purchases.stream().map(MembershipPurchaseResponseDto::membershipName))
            .containsExactlyInAnyOrder("강남 1개월권", "마포 1개월권");
    }

    @Test
    @DisplayName("시나리오 4: 이용권 예약 활성화 시스템")
    void scenario_MembershipScheduledActivationSystem() {
        // 1단계: 체육관에 이용권 등록
        MembershipResponseDto membership = membershipService.createMembership(
            gym1.getId(), owner1.getId(), "1개월 자유권", 80000, "1개월 자유 이용", 30
        );

        // 2단계: 여러 날짜로 예약 구매
        LocalDate today = LocalDate.now();
        MembershipPurchaseResponseDto purchase1 = membershipPurchaseService.purchase(
            membership.id(), user1.getId(), gym1.getId(), today.plusDays(1)
        );

        MembershipPurchaseResponseDto purchase2 = membershipPurchaseService.purchase(
            membership.id(), user1.getId(), gym1.getId(), today.plusDays(3)
        );

        // 3단계: 모든 이용권이 미활성화 상태인지 확인
        List<MembershipPurchaseResponseDto> notStarted = membershipPurchaseService.getNotStartedMemberships(user1.getId());
        assertThat(notStarted).hasSize(2);
        assertThat(notStarted.stream().allMatch(p -> !p.isActive())).isTrue();

        // 4단계: 첫 번째 이용권 활성화 (과거 시간으로 설정)
        MembershipPurchase entity1 = purchaseRepository.findById(purchase1.id()).orElseThrow();
        LocalDateTime activationTime = LocalDateTime.now().minusMinutes(10); // 10분 전에 활성화
        entity1.activate(activationTime);
        purchaseRepository.save(entity1);

        // 5단계: 활성화 후 상태 확인
        MembershipPurchaseResponseDto active = membershipPurchaseService.getMyActive(user1.getId());
        assertThat(active.id()).isEqualTo(purchase1.id());
        assertThat(active.isActive()).isTrue();
    }
}
