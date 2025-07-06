package org.example.fitpass.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.Image.entity.Image;
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
import org.example.fitpass.domain.point.dto.response.PointBalanceResponseDto;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("MembershipPurchaseService 단위 테스트")
class MembershipPurchaseServiceTest {

    @InjectMocks
    private MembershipPurchaseService membershipPurchaseService;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private MembershipPurchaseRepository purchaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private PointService pointService;

    private User mockUser;
    private Gym mockGym;
    private Membership mockMembership;
    private MembershipPurchase mockPurchase;

    @BeforeEach
    void setUp() {
        mockUser = new User(
            "user@test.com", null, "password123", "테스트사용자",
            "010-1234-5678", 25, "서울시 강남구",
            Gender.MAN, UserRole.USER, "LOCAL"
        );

        ReflectionTestUtils.setField(mockUser, "id", 1L);

        mockGym = Gym.of(
            List.of("gym1.jpg"),
            "테스트 헬스장",
            "02-1234-5678",
            "테스트용 헬스장",
            "서울시", "강남구", "테헤란로 100",
            LocalTime.of(6, 0), LocalTime.of(23, 0),
            "테스트 헬스장 설명",
            mockUser // 임시로 같은 사용자 사용
        );

        ReflectionTestUtils.setField(mockGym, "id", 1L);

        mockMembership = Membership.of(
            "1개월 자유 이용권",
            80000,
            "헬스장 자유 이용",
            30
        );
        mockMembership.assignToGym(mockGym);

        mockPurchase = new MembershipPurchase(
            mockMembership, mockGym, mockUser, LocalDateTime.now()
        );

        ReflectionTestUtils.setField(mockMembership, "id", 1L);
    }

    @Test
    @DisplayName("이용권 구매 - 성공")
    void purchase_Success() {
        // given
        LocalDate activationDate = LocalDate.now().plusDays(3);
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findByIdOrElseThrow(anyLong())).willReturn(mockMembership);
        given(purchaseRepository.save(any(MembershipPurchase.class))).willReturn(mockPurchase);
        PointBalanceResponseDto expectedPointResponse = new PointBalanceResponseDto(5000); // 예상 잔액
        given(pointService.usePoint(anyLong(), anyInt(), anyString())).willReturn(expectedPointResponse);

        // when
        MembershipPurchaseResponseDto result = membershipPurchaseService.purchase(
            1L, 1L, 1L, activationDate
        );

        // then
        assertThat(result.membershipName()).isEqualTo("1개월 자유 이용권");
        assertThat(result.price()).isEqualTo(80000);
        assertThat(result.durationInDays()).isEqualTo(30);

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(gymRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).findByIdOrElseThrow(1L);
        verify(pointService).usePoint(anyLong(), anyInt(), anyString());
        verify(purchaseRepository).save(any(MembershipPurchase.class));
    }

    @Test
    @DisplayName("이용권 구매 - 잘못된 체육관 이용권")
    void purchase_InvalidGymMembership() {
        // given
        // id만 있는 Gym 객체 (직접 생성자나 정적 팩토리 사용 불가한 경우 setId로 설정)
        Image mockImage = new Image("image.jpg");
        Gym differentGym = new Gym(
            List.of(mockImage),
            "다른 헬스장",
            "02-0000-0000",
            "다른 설명",
            "서울시", "중구", "중앙로 1",
            LocalTime.of(6, 0),
            LocalTime.of(23, 0),
            "설명",
            mockUser
        );
        setId(differentGym, 2L);  // 리플렉션 사용한 유틸 메서드로 ID 설정

        // 해당 Gym에 속한 Membership 객체 생성
        Membership membershipOfDifferentGym = Membership.of(
            "다른 헬스장 이용권",
            50000,
            "다른 설명",
            30
        );
        membershipOfDifferentGym.assignToGym(differentGym);
        setId(membershipOfDifferentGym, 1L);

        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findByIdOrElseThrow(anyLong())).willReturn(membershipOfDifferentGym);

        // when & then
        assertThatThrownBy(() -> membershipPurchaseService.purchase(
            1L, 1L, 1L, LocalDate.now().plusDays(1)
        )).isInstanceOf(BaseException.class)
          .hasMessageContaining(ExceptionCode.INVALID_GYM_MEMBERSHIP.getMessage());

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(gymRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).findByIdOrElseThrow(1L);
    }

    @Test
    @DisplayName("이용권 구매 - 과거 날짜 활성화")
    void purchase_InvalidActivationDatePast() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findByIdOrElseThrow(anyLong())).willReturn(mockMembership);

        // when & then
        assertThatThrownBy(() -> membershipPurchaseService.purchase(
            1L, 1L, 1L, pastDate
        )).isInstanceOf(BaseException.class)
          .hasMessageContaining(ExceptionCode.INVALID_ACTIVATION_DATE_PAST.getMessage());
    }

    @Test
    @DisplayName("이용권 구매 - 너무 먼 미래 날짜 활성화")
    void purchase_InvalidActivationDateTooFar() {
        // given
        LocalDate farFutureDate = LocalDate.now().plusDays(10);
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findByIdOrElseThrow(anyLong())).willReturn(mockMembership);

        // when & then
        assertThatThrownBy(() -> membershipPurchaseService.purchase(
            1L, 1L, 1L, farFutureDate
        )).isInstanceOf(BaseException.class)
          .hasMessageContaining(ExceptionCode.INVALID_ACTIVATION_DATE_TOO_FAR.getMessage());
    }

    @Test
    @DisplayName("미활성화 이용권 조회 - 성공")
    void getNotStartedMemberships_Success() {
        // given
        List<MembershipPurchase> notStartedPurchases = List.of(mockPurchase);
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);
        given(purchaseRepository.findAllNotStartedByUser(any(User.class))).willReturn(notStartedPurchases);

        // when
        List<MembershipPurchaseResponseDto> result = membershipPurchaseService.getNotStartedMemberships(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).membershipName()).isEqualTo("1개월 자유 이용권");

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(purchaseRepository).findAllNotStartedByUser(mockUser);
    }

    @Test
    @DisplayName("구매 이력 조회 - 성공")
    void getMyPurchases_Success() {
        // given
        List<MembershipPurchase> purchases = List.of(mockPurchase);
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);
        given(purchaseRepository.findAllByUser(any(User.class))).willReturn(purchases);

        // when
        List<MembershipPurchaseResponseDto> result = membershipPurchaseService.getMyPurchases(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).membershipName()).isEqualTo("1개월 자유 이용권");

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(purchaseRepository).findAllByUser(mockUser);
    }

    @Test
    @DisplayName("현재 활성 이용권 조회 - 성공")
    void getMyActive_Success() {
        // given
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        ReflectionTestUtils.setField(mockGym, "id", 1L);
        ReflectionTestUtils.setField(mockMembership, "id", 1L);

        // 고정된 시간 사용
        LocalDateTime fixedTime = LocalDateTime.now().minusHours(1); // 1시간 전에 활성화

        MembershipPurchase activePurchase = new MembershipPurchase(
            mockMembership, mockGym, mockUser, LocalDateTime.now()
        );
        activePurchase.activate(fixedTime);

        List<MembershipPurchase> activePurchases = List.of(activePurchase);
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);
        given(purchaseRepository.findAllActiveByUser(any(User.class), any(LocalDateTime.class)))
            .willReturn(activePurchases);

        // when
        MembershipPurchaseResponseDto result = membershipPurchaseService.getMyActive(1L);

        // then
        assertThat(result.membershipName()).isEqualTo("1개월 자유 이용권");
        assertThat(result.isActive()).isTrue();

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(purchaseRepository).findAllActiveByUser(any(User.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("현재 활성 이용권 조회 - 활성 이용권 없음")
    void getMyActive_NotActive() {
        // given
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);
        given(purchaseRepository.findAllActiveByUser(any(User.class), any(LocalDateTime.class)))
            .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> membershipPurchaseService.getMyActive(1L))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining(ExceptionCode.MEMBERSHIP_NOT_ACTIVE.getMessage());

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(purchaseRepository).findAllActiveByUser(any(User.class), any(LocalDateTime.class));
    }

    private void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }
}
