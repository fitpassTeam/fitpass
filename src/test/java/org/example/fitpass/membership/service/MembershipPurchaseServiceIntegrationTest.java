package org.example.fitpass.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.membership.dto.response.MembershipPurchaseResponseDto;
import org.example.fitpass.domain.membership.entity.Membership;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;
import org.example.fitpass.domain.membership.repository.MembershipPurchaseRepository;
import org.example.fitpass.domain.membership.repository.MembershipRepository;
import org.example.fitpass.domain.membership.service.MembershipPurchaseService;
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
@DisplayName("MembershipPurchaseService 통합 테스트")
class MembershipPurchaseServiceIntegrationTest {

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

    private User savedUser;
    private User savedOwner;
    private Gym savedGym;
    private Membership savedMembership;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        purchaseRepository.deleteAll();
        membershipRepository.deleteAll();
        pointRepository.deleteAll();
        gymRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 데이터 설정
        savedUser = userRepository.save(new User(
            "user@test.com",
            null,
            "password123",
            "일반사용자",
            "010-1234-5678",
            25,
            "서울시 강남구",
            Gender.MAN,
            UserRole.USER,
            "LOCAL"
        ));

        savedOwner = userRepository.save(new User(
            "owner@test.com",
            null,
            "password123",
            "체육관사장",
            "010-9876-5432",
            35,
            "서울시 서초구",
            Gender.WOMAN,
            UserRole.OWNER,
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

        savedMembership = membershipRepository.save(Membership.of(
            "1개월 자유 이용권",
            80000,
            "헬스장 자유 이용 가능",
            30
        ));
        savedMembership.assignToGym(savedGym);
        savedMembership = membershipRepository.save(savedMembership); // 수정: 다시 할당

        // 사용자에게 충분한 포인트 지급
        Point point = pointRepository.save(new Point(savedUser, 200000, "테스트 포인트 지급", 200000, PointType.CHARGE));
        savedUser.updatePointBalance(200000); // 수정: 사용자 잔액 업데이트
        savedUser = userRepository.save(savedUser); // 수정: 업데이트된 사용자 저장
    }

    @Test
    @DisplayName("이용권 구매 통합 테스트 - 성공")
    void purchase_Integration_Success() {
        // when
        MembershipPurchaseResponseDto result = membershipPurchaseService.purchase(
            savedMembership.getId(),
            savedUser.getId(),
            savedGym.getId(),
            LocalDate.now().plusDays(3)
        );

        // then
        assertThat(result.membershipName()).isEqualTo("1개월 자유 이용권");
        assertThat(result.price()).isEqualTo(80000);
        assertThat(result.durationInDays()).isEqualTo(30);
        assertThat(result.isActive()).isFalse(); // 아직 활성화되지 않음

        // 데이터베이스에 저장되었는지 확인
        List<MembershipPurchase> purchases = purchaseRepository.findAllByUser(savedUser);
        assertThat(purchases).hasSize(1);
        assertThat(purchases.get(0).getMembership().getName()).isEqualTo("1개월 자유 이용권");
    }

    @Test
    @DisplayName("현재 활성 이용권 조회 통합 테스트 - 성공")
    void getMyActive_Integration_Success() {
        // given - 활성 이용권 생성 (과거 시간으로 활성화)
        MembershipPurchase activePurchase = new MembershipPurchase(
            savedMembership, savedGym, savedUser, LocalDateTime.now()
        );
        LocalDateTime activationTime = LocalDateTime.now().minusMinutes(10); // 10분 전에 활성화
        activePurchase.activate(activationTime);
        purchaseRepository.save(activePurchase);

        // when
        MembershipPurchaseResponseDto result = membershipPurchaseService.getMyActive(savedUser.getId());

        // then
        assertThat(result.membershipName()).isEqualTo("1개월 자유 이용권");
        assertThat(result.isActive()).isTrue();
        assertThat(result.startDate()).isNotNull();
        assertThat(result.endDate()).isNotNull();
    }

    @Test
    @DisplayName("현재 활성 이용권 조회 통합 테스트 - 활성 이용권 없음")
    void getMyActive_Integration_NotActive() {
        // when & then (활성 이용권 없이 조회)
        assertThatThrownBy(() -> membershipPurchaseService.getMyActive(savedUser.getId()))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining(ExceptionCode.MEMBERSHIP_NOT_ACTIVE.getMessage());
    }

    @Test
    @DisplayName("과거 날짜로 이용권 구매 시도")
    void purchase_Integration_PastDate() {
        // when & then
        assertThatThrownBy(() -> membershipPurchaseService.purchase(
            savedMembership.getId(), savedUser.getId(), savedGym.getId(), LocalDate.now().minusDays(1)
        )).isInstanceOf(BaseException.class)
          .hasMessageContaining(ExceptionCode.INVALID_ACTIVATION_DATE_PAST.getMessage());
    }
}
