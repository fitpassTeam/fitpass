package org.example.fitpass.reservation;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.notify.NotificationType;
import org.example.fitpass.domain.notify.service.NotifyService;
import org.example.fitpass.domain.point.dto.response.PointBalanceResponseDto;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.reservation.dto.response.UpdateReservationResponseDto;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.repository.ReservationRepository;
import org.example.fitpass.domain.reservation.service.ReservationService;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotifyService notifyService;

    @Mock
    private PointService pointService;

    @Test
    void 예약_가능_시간_조회_성공() {
        // given
        Long userId = 1L;
        Long gymId = 2L;
        Long trainerId = 3L;
        LocalDate date = LocalDate.of(2025, 6, 30);

        User testUser = new User(); // 필요한 필드 세팅
        Gym gym = Gym.of(
            List.of(), // 이미지 URL 문자열 리스트
            "헬스장 이름",
            "010-1234-5678",
            "헬스장 설명",
            "서울",
            "강남구",
            "테헤란로 332",
            LocalTime.of(10, 0),
            LocalTime.of(14, 0),
            "간단 요약",
            testUser
        );
        Trainer trainer = new Trainer();

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(testUser);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(trainer);

        List<LocalTime> reserved = List.of(LocalTime.of(11, 0));
        given(reservationRepository.findReservedTimesByTrainerAndDate(trainer, date)).willReturn(reserved);

        // when
        List<LocalTime> result = reservationService.getAvailableTimes(userId, gymId, trainerId, date);

        // then
        assertThat(result).containsExactly(
            LocalTime.of(10, 0),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0)
        );
    }

    @Test
    void 예약_수정_성공_케이스() {
        // given
        Long userId = 1L;
        Long gymId = 1L;
        Long trainerId = 1L;
        Long reservationId = 1L;
        LocalDate today = LocalDate.of(2025, 6, 30);
        LocalDate existingReservationDate = today.plusDays(5);
        LocalDate newReservationDate = today.plusDays(6);
        LocalTime newTime = LocalTime.of(12, 0);

        // 유저
        User user = new User("test@email.com", "홍길동", "GOOGLE");
        ReflectionTestUtils.setField(user, "id", 1L);

        // 헬스장
        Gym gym = Gym.of(
            List.of(), "헬스장", "1234", "내용", "서울", "강남", "주소",
            LocalTime.of(9, 0), LocalTime.of(22, 0), "요약", user
        );
        ReflectionTestUtils.setField(gym, "id", gymId);

        // 트레이너
        List<Image> images = List.of(new Image("img1.jpg"), new Image("img2.jpg"));
        Trainer trainer = new Trainer(images, "김트레이너", 50000, "PT 전문가", "5년 경력");
        trainer.assignToGym(gym);
        ReflectionTestUtils.setField(trainer, "id", trainerId);

        // 예약 (mock 객체)
        Reservation reservation = mock(Reservation.class);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getReservationDate()).thenReturn(existingReservationDate);
        when(reservation.getReservationStatus()).thenReturn(ReservationStatus.PENDING);
        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getGym()).thenReturn(gym);
        when(reservation.getTrainer()).thenReturn(trainer);
        when(reservation.updateReservation(newReservationDate, newTime, ReservationStatus.PENDING)).thenReturn(reservation);

        // 의존성 주입 결과 정의
        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(trainer);
        given(reservationRepository.findByIdOrElseThrow(reservationId)).willReturn(reservation);
        given(reservationRepository.existsByTrainerAndReservationDateAndReservationTimeAndIdNot(
            trainer, newReservationDate, newTime, reservationId)).willReturn(false);

        doNothing().when(notifyService).send(any(User.class), any(NotificationType.class), anyString(), anyString());

        // when
        UpdateReservationResponseDto response = reservationService.updateReservation(
            newReservationDate,
            newTime,
            ReservationStatus.PENDING,
            userId,
            gymId,
            trainerId,
            reservationId
        );

        // then
        assertThat(response).isNotNull();
    }

    @Test
    void 예약_취소_성공_케이스() {
        // given
        Long userId = 1L;
        Long gymId = 1L;
        Long trainerId = 1L;
        Long reservationId = 1L;

        LocalDate today = LocalDate.now();
        LocalDate reservationDate = today.plusDays(7);
        LocalTime reservationTime = LocalTime.of(12, 0);

        User user = new User("test@email.com", "홍길동", "GOOGLE");
        ReflectionTestUtils.setField(user, "id", userId);

        Gym gym = Gym.of(List.of(), "헬스장", "1234", "내용", "서울", "강남", "주소",
            LocalTime.of(9, 0), LocalTime.of(22, 0), "요약", user);
        ReflectionTestUtils.setField(gym, "id", gymId);

        Trainer trainer = new Trainer(List.of(), "김트레이너", 50000, "PT 전문가", "5년 경력");
        trainer.assignToGym(gym);
        ReflectionTestUtils.setField(trainer, "id", trainerId);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getReservationDate()).thenReturn(reservationDate);
        when(reservation.getReservationTime()).thenReturn(reservationTime);
        when(reservation.getReservationStatus()).thenReturn(ReservationStatus.PENDING);
        when(reservation.getId()).thenReturn(reservationId);

        // ✅ notifyService 호출되므로 반드시 stubbing 필요
        doNothing().when(notifyService).send(any(), any(), anyString(), anyString());

        given(pointService.refundPoint(eq(userId), eq(trainer.getPrice()), anyString()))
            .willReturn(new PointBalanceResponseDto(10000));

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(user);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(trainer);
        given(reservationRepository.findByIdOrElseThrow(reservationId)).willReturn(reservation);

        // when
        reservationService.cancelReservation(userId, gymId, trainerId, reservationId);

        // then
        verify(pointService).refundPoint(eq(userId), eq(trainer.getPrice()), anyString());
        verify(notifyService, times(2)).send(any(), eq(NotificationType.RESERVATION), anyString(), contains("/gyms/"));
        verify(reservation).cancelReservation();
    }

    @Test
    void 예약_승인_성공_케이스() {
        // given
        Long userId = 1L;
        Long gymId = 1L;
        Long trainerId = 1L;
        Long reservationId = 1L;

        User owner = new User("owner@email.com", "오너", "GOOGLE");
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(owner, "userRole", UserRole.OWNER);

        Gym gym = Gym.of(List.of(), "헬스장", "1234", "내용", "서울", "강남", "주소",
            LocalTime.of(9, 0), LocalTime.of(22, 0), "요약", owner);
        ReflectionTestUtils.setField(gym, "id", gymId);

        Trainer trainer = new Trainer(List.of(), "트레이너", 50000, "소개", "경력");
        trainer.assignToGym(gym);
        ReflectionTestUtils.setField(trainer, "id", trainerId);

        User member = new User("user@email.com", "회원", "GOOGLE");
        ReflectionTestUtils.setField(member, "id", 999L);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getUser()).thenReturn(member);
        when(reservation.getReservationDate()).thenReturn(LocalDate.now().plusDays(5));
        when(reservation.getReservationTime()).thenReturn(LocalTime.of(10, 0));
        when(reservation.getReservationStatus()).thenReturn(ReservationStatus.PENDING);
        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getGym()).thenReturn(gym);

        // stub
        given(userRepository.findByIdOrElseThrow(userId)).willReturn(owner);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(trainer);
        given(reservationRepository.findByIdOrElseThrow(reservationId)).willReturn(reservation);

        // when
        reservationService.confirmReservation(userId, gymId, trainerId, reservationId);

        // then
        verify(reservation).updateReservation(any(), any(), eq(ReservationStatus.CONFIRMED));
        verify(notifyService, times(2)).send(any(), eq(NotificationType.RESERVATION), anyString(), contains("/gyms/"));
    }

    @Test
    void 예약_거부_성공_케이스() {
        // given
        Long userId = 1L;
        Long gymId = 1L;
        Long trainerId = 1L;
        Long reservationId = 1L;

        User owner = new User("owner@email.com", "오너", "GOOGLE");
        ReflectionTestUtils.setField(owner, "id", userId);
        ReflectionTestUtils.setField(owner, "userRole", UserRole.OWNER);

        User user = new User("user@email.com", "회원", "GOOGLE");
        ReflectionTestUtils.setField(user, "id", 2L);

        Gym gym = Gym.of(List.of(), "헬스장", "1234", "내용", "서울", "강남", "주소",
            LocalTime.of(9, 0), LocalTime.of(22, 0), "요약", owner);
        ReflectionTestUtils.setField(gym, "id", gymId);

        Trainer trainer = new Trainer(List.of(), "트레이너", 50000, "설명", "경력");
        trainer.assignToGym(gym);
        ReflectionTestUtils.setField(trainer, "id", trainerId);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getReservationStatus()).thenReturn(ReservationStatus.PENDING);
        when(reservation.getGym()).thenReturn(gym);
        when(reservation.getId()).thenReturn(reservationId);

        // stub repository
        given(userRepository.findByIdOrElseThrow(userId)).willReturn(owner);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(trainer);
        given(reservationRepository.findByIdOrElseThrow(reservationId)).willReturn(reservation);

        // stub point refund
        given(pointService.refundPoint(eq(user.getId()), eq(trainer.getPrice()), anyString()))
            .willReturn(new PointBalanceResponseDto(10000));

        // stub notifyService
        doNothing().when(notifyService).send(any(), any(), anyString(), anyString());

        // when
        reservationService.rejectReservation(userId, gymId, trainerId, reservationId);

        // then
        verify(pointService).refundPoint(eq(user.getId()), eq(trainer.getPrice()), anyString());
        verify(reservation).cancelReservation();
        verify(notifyService, times(2)).send(any(), eq(NotificationType.RESERVATION), anyString(), contains("/gyms/"));
    }






}

