package org.example.fitpass.membership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("MembershipService 단위 테스트")
class MembershipServiceTest {

    @InjectMocks
    private MembershipService membershipService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private MembershipRepository membershipRepository;

    private User mockOwner;
    private User mockUser;
    private Gym mockGym;
    private Membership mockMembership;

    @BeforeEach
    void setUp() {
        mockOwner = new User(
            "owner@test.com", null, "password123", "체육관사장",
            "010-1234-5678", 35, "서울시 강남구",
            Gender.MAN, UserRole.OWNER, "LOCAL"
        );

        mockUser = new User(
            "user@test.com", null, "password123", "일반사용자",
            "010-9876-5432", 25, "서울시 서초구",
            Gender.WOMAN, UserRole.USER, "LOCAL"
        );

        ReflectionTestUtils.setField(mockOwner, "id", 1L);

        mockGym = Gym.of(
            List.of("gym1.jpg"),
            "테스트 헬스장",
            "02-1234-5678",
            "테스트용 헬스장",
            "서울시", "강남구", "테헤란로 100",
            LocalTime.of(6, 0), LocalTime.of(23, 0),
            "테스트 헬스장 설명",
            mockOwner
        );

        ReflectionTestUtils.setField(mockGym, "id", 1L);

        mockMembership = Membership.of(
            "1개월 자유 이용권",
            80000,
            "헬스장 자유 이용",
            30
        );
        mockMembership.assignToGym(mockGym);
        ReflectionTestUtils.setField(mockMembership, "id", 1L);
    }

    @Test
    @DisplayName("이용권 생성 - 성공")
    void createMembership_Success() {
        // given
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockOwner);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.save(any(Membership.class))).willReturn(mockMembership);

        // when
        MembershipResponseDto result = membershipService.createMembership(
            1L, 1L, "1개월 자유 이용권", 80000, "헬스장 자유 이용", 30
        );

        // then
        assertThat(result.name()).isEqualTo("1개월 자유 이용권");
        assertThat(result.price()).isEqualTo(80000);
        assertThat(result.durationInDays()).isEqualTo(30);

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(gymRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).save(any(Membership.class));
    }

    @Test
    @DisplayName("이용권 생성 - 체육관 소유자가 아닌 경우")
    void createMembership_NotGymOwner() {
        // given
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);

        // when & then
        assertThatThrownBy(() -> membershipService.createMembership(
            1L, 2L, "1개월 자유 이용권", 80000, "헬스장 자유 이용", 30
        )).isInstanceOf(BaseException.class)
          .hasMessageContaining(ExceptionCode.NOT_GYM_OWNER.getMessage());

        verify(userRepository).findByIdOrElseThrow(2L);
        verify(gymRepository, times(0)).findByIdOrElseThrow(anyLong());
    }

    @Test
    @DisplayName("이용권 생성 - 다른 체육관 소유자인 경우")
    void createMembership_DifferentGymOwner() {
        // given
        User differentOwner = new User(
            "different@test.com", null, "password123", "다른사장",
            "010-5555-5555", 40, "서울시 마포구",
            Gender.MAN, UserRole.OWNER, "LOCAL"
        );

        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(differentOwner);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);

        // when & then
        assertThatThrownBy(() -> membershipService.createMembership(
            1L, 3L, "1개월 자유 이용권", 80000, "헬스장 자유 이용", 30
        )).isInstanceOf(BaseException.class)
          .hasMessageContaining(ExceptionCode.NOT_GYM_OWNER.getMessage());

        verify(userRepository).findByIdOrElseThrow(3L);
        verify(gymRepository).findByIdOrElseThrow(1L);
    }

    @Test
    @DisplayName("체육관 이용권 목록 조회 - 성공")
    void getAllByGym_Success() {
        // given
        List<Membership> memberships = List.of(mockMembership);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findAllByGym(any(Gym.class))).willReturn(memberships);

        // when
        List<MembershipResponseDto> result = membershipService.getAllByGym(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("1개월 자유 이용권");
        assertThat(result.get(0).price()).isEqualTo(80000);

        verify(gymRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).findAllByGym(mockGym);
    }

    @Test
    @DisplayName("이용권 상세 조회 - 성공")
    void getMembershipById_Success() {
        // given
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findByIdOrElseThrow(anyLong())).willReturn(mockMembership);

        // when
        MembershipResponseDto result = membershipService.getMembershipById(1L, 1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("1개월 자유 이용권");
        assertThat(result.price()).isEqualTo(80000);

        verify(gymRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).findByIdOrElseThrow(1L);
    }

    @Test
    @DisplayName("이용권 상세 조회 - 다른 체육관의 이용권")
    void getMembershipById_InvalidGymMembership() {
        // given
        Gym differentGym = Gym.of(
            List.of("gym2.jpg"),
            "다른 헬스장",
            "02-5555-5555",
            "다른 테스트용 헬스장",
            "서울시", "마포구", "월드컵로 100",
            LocalTime.of(7, 0), LocalTime.of(22, 0),
            "다른 헬스장 설명",
            mockOwner
        );
        ReflectionTestUtils.setField(differentGym, "id", 2L); // 다른 ID로 설정


        Membership membershipOfDifferentGym = Membership.of(
            "다른 헬스장 이용권",
            90000,
            "다른 헬스장 이용",
            30
        );
        membershipOfDifferentGym.assignToGym(differentGym);

        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findByIdOrElseThrow(anyLong())).willReturn(membershipOfDifferentGym);

        // when & then
        assertThatThrownBy(() -> membershipService.getMembershipById(1L, 1L))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining(ExceptionCode.INVALID_GYM_MEMBERSHIP.getMessage());

        verify(gymRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).findByIdOrElseThrow(1L);
    }

    @Test
    @DisplayName("이용권 수정 - 성공")
    void updateMembership_Success() {
        // given
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockOwner);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findByIdOrElseThrow(anyLong())).willReturn(mockMembership);

        // when
        MembershipResponseDto result = membershipService.updateMembership(
            1L, 1L, 1L, "수정된 이용권", 90000, "수정된 내용", 45
        );

        // then
        assertThat(result.name()).isEqualTo("수정된 이용권");
        assertThat(result.price()).isEqualTo(90000);
        assertThat(result.content()).isEqualTo("수정된 내용");
        assertThat(result.durationInDays()).isEqualTo(45);

        verify(userRepository).findByIdOrElseThrow(1L);
        verify(gymRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).findByIdOrElseThrow(1L);
    }

    @Test
    @DisplayName("이용권 삭제 - 성공")
    void deleteMembership_Success() {
        // given
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockOwner);
        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(mockGym);
        given(membershipRepository.findByIdOrElseThrow(anyLong())).willReturn(mockMembership);
        doNothing().when(membershipRepository).delete(any(Membership.class));

        // when
        membershipService.deleteMembership(1L, 1L, 1L);

        // then
        verify(userRepository).findByIdOrElseThrow(1L);
        verify(gymRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).findByIdOrElseThrow(1L);
        verify(membershipRepository).delete(mockMembership);
    }

    @Test
    @DisplayName("이용권 삭제 - 권한 없음")
    void deleteMembership_NotAuthorized() {
        // given
        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(mockUser);

        // when & then
        assertThatThrownBy(() -> membershipService.deleteMembership(1L, 1L, 2L))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining(ExceptionCode.NOT_GYM_OWNER.getMessage());

        verify(userRepository).findByIdOrElseThrow(2L);
        verify(membershipRepository, times(0)).delete(any());
    }
}
