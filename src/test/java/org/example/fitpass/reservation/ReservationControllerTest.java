package org.example.fitpass.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.reservation.dto.request.ReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.request.UpdateReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.response.*;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.service.ReservationService;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        User user = new User(
            "test@test.com",
            "profile.jpg",
            "password",
            "테스트유저",
            "010-1234-5678",
            30,
            "서울시 강남구",
            Gender.MAN,
            UserRole.USER
        );
        userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Test
    void getAvailableTimes_success() throws Exception {
        List<LocalTime> times = List.of(LocalTime.of(10, 0), LocalTime.of(11, 0));
        given(reservationService.getAvailableTimes(anyLong(), anyLong(), anyLong(), any(LocalDate.class)))
            .willReturn(times);

        mockMvc.perform(get("/gyms/1/trainers/1/available-times")
                .param("date", "2025-07-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0]").value("10:00:00"))
            .andExpect(jsonPath("$.data[1]").value("11:00:00"));
    }

    @Test
    void createReservation_success() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(3); // 미래 날짜 사용
        ReservationRequestDto requestDto = new ReservationRequestDto(futureDate, LocalTime.of(14, 0));

        ReservationResponseDto responseDto = new ReservationResponseDto(
            1L,              // reservationId
            2L,              // userId
            3L,              // gymId
            4L,              // trainerId
            futureDate,      // reservationDate - 요청과 맞추기
            LocalTime.of(14, 0),
            ReservationStatus.CONFIRMED,
            LocalDateTime.now()
        );

        // 파라미터 타입을 명확히 지정해 모킹
        given(reservationService.createReservation(
            any(LocalDate.class),
            any(LocalTime.class),
            anyLong(),
            anyLong(),
            anyLong()
        )).willReturn(responseDto);

        mockMvc.perform(post("/gyms/1/trainers/1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.reservationId").value(responseDto.reservationId()))
            .andExpect(jsonPath("$.data.reservationDate").value(responseDto.reservationDate().toString()))
            .andExpect(jsonPath("$.data.reservationTime").value(responseDto.reservationTime().toString()))
            .andExpect(jsonPath("$.data.reservationStatus").value(responseDto.reservationStatus().toString()));
    }



    @Test
    void updateReservation_success() throws Exception {
        UpdateReservationRequestDto updateDto = new UpdateReservationRequestDto(
            LocalDate.of(2025, 7, 2),
            LocalTime.of(15, 0),
            ReservationStatus.CANCELLED
        );

        UpdateReservationResponseDto responseDto = new UpdateReservationResponseDto(
            1L,
            2L,
            3L,
            4L,
            LocalDate.of(2025, 7, 2),
            LocalTime.of(15, 0),
            ReservationStatus.CANCELLED,
            LocalDateTime.now()
        );

        given(reservationService.updateReservation(
            any(), any(), any(ReservationStatus.class),
            anyLong(), anyLong(), anyLong(), anyLong()))
            .willReturn(responseDto);

        mockMvc.perform(patch("/gyms/1/trainers/1/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.reservationId").value(1L))
            .andExpect(jsonPath("$.data.reservationDate").value("2025-07-02"))
            .andExpect(jsonPath("$.data.reservationTime").value("15:00:00"))
            .andExpect(jsonPath("$.data.reservationStatus").value("CANCELLED"));
    }

    @Test
    void cancelReservation_success() throws Exception {
        doNothing().when(reservationService).cancelReservation(anyLong(), anyLong(), anyLong(), anyLong());

        mockMvc.perform(delete("/gyms/1/trainers/1/reservations/1"))
            .andExpect(status().isOk());
    }

    @Test
    void getTrainerReservation_success() throws Exception {
        List<TrainerReservationResponseDto> list = List.of(
            new TrainerReservationResponseDto(
                1L,
                LocalDate.now().plusDays(1),
                LocalTime.of(14,0),
                ReservationStatus.CONFIRMED,
                LocalDateTime.now(),
                new TrainerReservationResponseDto.GymInfo(10L, "헬스장 이름", "서울 강남구", "010-1234-5678"),
                new TrainerReservationResponseDto.UserInfo(20L, "사용자 이름", "test@test.com", "010-1111-2222")
            )
        );
        given(reservationService.getTrainerReservation(anyLong(), anyLong(), anyLong()))
            .willReturn(list);

        mockMvc.perform(get("/gyms/1/trainers/1/reservations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].reservationId").value(1L));
    }


    @Test
    void getGymAllReservations_success() throws Exception {
        List<AllGymReservationResponseDto> list = List.of(
            new AllGymReservationResponseDto(
                "홍길동",                  // userName (String)
                1L,                        // reservationId (Long)
                "김트레이너",               // trainerName (String)
                LocalDate.of(2025, 7, 1),  // reservationDate (LocalDate)
                LocalTime.of(14, 0),       // reservationTime (LocalTime)
                ReservationStatus.CONFIRMED, // status (enum)
                100L                       // trainerId (Long)
            )
        );
        given(reservationService.getGymAllReservations(anyLong(), anyLong()))
            .willReturn(list);

        mockMvc.perform(get("/gyms/1/reservations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].reservationId").value(1L));
    }

    @Test
    void getUserReservations_success() throws Exception {
        List<UserReservationResponseDto> list = List.of(
            new UserReservationResponseDto(
                1L,                              // reservationId
                LocalDate.of(2025,7,1),          // reservationDate
                LocalTime.of(14,0),              // reservationTime
                ReservationStatus.CONFIRMED,     // status
                LocalDateTime.now(),             // createdAt (추가 필요)
                new UserReservationResponseDto.GymInfo(   // gym 정보
                    10L,
                    "헬스장 이름",
                    "서울시 강남구",
                    "010-1234-5678"
                ),
                new UserReservationResponseDto.TrainerInfo(  // trainer 정보
                    20L,
                    "트레이너 이름",
                    "전문 트레이너",
                    50000
                )
            )
        );
        given(reservationService.getUserReservations(anyLong()))
            .willReturn(list);

        mockMvc.perform(get("/users/reservations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].reservationId").value(1L));
    }

    @Test
    void getReservation_success() throws Exception {
        GetReservationResponseDto responseDto = new GetReservationResponseDto(
            1L,                            // reservationId
            2L,                            // userId (예시)
            3L,                            // gymId (예시)
            4L,                            // trainerId (예시)
            LocalDate.of(2025, 7, 1),     // reservationDate
            LocalTime.of(14, 0),          // reservationTime
            ReservationStatus.CONFIRMED,   // enum 타입
            LocalDateTime.now(),           // createdAt (예시)
            LocalDateTime.now()            // updatedAt (예시)
        );
        given(reservationService.getReservation(anyLong(), anyLong()))
            .willReturn(responseDto);

        mockMvc.perform(get("/reservations/1"))
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.reservationId").value(1L))
            .andExpect(jsonPath("$.data.reservationStatus").value("CONFIRMED"));
    }

    @Test
    void confirmReservation_success() throws Exception {
        doNothing().when(reservationService).confirmReservation(anyLong(), anyLong(), anyLong(), anyLong());

        mockMvc.perform(patch("/gyms/1/trainers/1/reservations/1/confirm"))
            .andExpect(status().isOk());
    }

    @Test
    void rejectReservation_success() throws Exception {
        doNothing().when(reservationService).rejectReservation(anyLong(), anyLong(), anyLong(), anyLong());

        mockMvc.perform(patch("/gyms/1/trainers/1/reservations/1/reject"))
            .andExpect(status().isOk());
    }
}
