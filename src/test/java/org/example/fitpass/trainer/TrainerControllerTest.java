/*
package org.example.fitpass.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.config.RedisDao;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.trainer.controller.TrainerController;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerRequestDto;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerUpdateRequestDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;
import org.example.fitpass.domain.trainer.service.TrainerService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(TrainerController.class)
@DisplayName("TrainerController 단위 테스트")
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerService trainerService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "jpaAuditingHandler")
    private Object auditingHandler;

    @MockBean
    @Qualifier("customStringRedisTemplate") // 문제되는 의존성 대상
    private RedisTemplate<String, String> redisTemplate;

    // 이 RedisTemplate을 사용하는 서비스도 자동 주입됨 (RedisService 등)
    @MockBean
    private RedisService redisService;

    private TrainerRequestDto trainerRequestDto;
    private TrainerResponseDto trainerResponseDto;
    private TrainerDetailResponseDto trainerDetailResponseDto;

    @BeforeEach
    void setUp() {
        // 🔹 인증된 사용자 직접 생성 (User 객체)
        User ownerUser = new User(
            "owner@test.com",
            null,
            "password123",
            "체육관사장",
            "010-1234-5678",
            30,
            "서울시 강남구",
            Gender.MAN,
            UserRole.OWNER,
            "LOCAL"
        );
        setId(ownerUser, 1L); // 🔑 ID 수동 설정 메서드 필요 (리플렉션)

        // 🔹 CustomUserDetails로 Wrapping
        CustomUserDetails userDetails = new CustomUserDetails(ownerUser);

        // 🔹 인증 토큰 생성 및 SecurityContext에 등록
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        // 이미지 부분을 빈 리스트로 처리
        trainerRequestDto = new TrainerRequestDto(
            "김트레이너", 50000, "전문 트레이너", "5년 경력", Collections.emptyList()
        );

        trainerResponseDto = new TrainerResponseDto(
            1L, "김트레이너", 50000, "전문 트레이너", TrainerStatus.ACTIVE, "5년 경력", Collections.emptyList()
        );

        trainerDetailResponseDto = new TrainerDetailResponseDto(
            "김트레이너", 50000, "전문 트레이너", "5년 경력", 
            TrainerStatus.ACTIVE, Collections.emptyList(), LocalDateTime.now()
        );
    }

    void setupSecurityContext() {
        User mockUser = new User(...); // 최소 필드만
        setId(mockUser, 1L);

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("트레이너 생성 API")
    class CreateTrainerApi {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공: 트레이너 생성")
        void createTrainer_Success() throws Exception {
            // given
            given(trainerService.createTrainer(anyLong(), anyLong(), anyString(), 
                anyInt(), anyString(), anyString(), anyList()))
                .willReturn(trainerResponseDto);

            // when & then
            mockMvc.perform(post("/gyms/{gymId}/trainers", 1L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(trainerRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("김트레이너"))
                .andExpect(jsonPath("$.data.price").value(50000))
                .andExpect(jsonPath("$.data.trainerStatus").value("ACTIVE"));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 잘못된 요청 데이터 - 빈 이름")
        void createTrainer_InvalidRequest_EmptyName() throws Exception {
            // given
            TrainerRequestDto invalidDto = new TrainerRequestDto(
                "", 50000, "내용", "경력", Collections.emptyList()
            );

            // when & then
            mockMvc.perform(post("/gyms/{gymId}/trainers", 1L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 잘못된 요청 데이터 - 음수 가격")
        void createTrainer_InvalidRequest_NegativePrice() throws Exception {
            // given
            TrainerRequestDto invalidDto = new TrainerRequestDto(
                "트레이너", -1000, "내용", "경력", Collections.emptyList()
            );

            // when & then
            mockMvc.perform(post("/gyms/{gymId}/trainers", 1L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 권한 없음")
        void createTrainer_Unauthorized() throws Exception {
            // given
            given(trainerService.createTrainer(anyLong(), anyLong(), anyString(), 
                anyInt(), anyString(), anyString(), anyList()))
                .willThrow(new BaseException(ExceptionCode.NOT_GYM_OWNER));

            // when & then
            mockMvc.perform(post("/gyms/{gymId}/trainers", 1L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(trainerRequestDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 존재하지 않는 체육관")
        void createTrainer_GymNotFound() throws Exception {
            // given
            given(trainerService.createTrainer(anyLong(), anyLong(), anyString(), 
                anyInt(), anyString(), anyString(), anyList()))
                .willThrow(new BaseException(ExceptionCode.GYM_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/gyms/{gymId}/trainers", 999L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(trainerRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("트레이너 조회 API")
    class GetTrainerApi {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공: 트레이너 전체 조회")
        void getAllTrainer_Success() throws Exception {
            // given
            Page<TrainerResponseDto> trainerPage = new PageImpl<>(
                List.of(trainerResponseDto), PageRequest.of(0, 10), 1);
            given(trainerService.getAllTrainer(anyLong(), any())).willReturn(trainerPage);

            // when & then
            mockMvc.perform(get("/gyms/{gymId}/trainers", 1L)
                    .param("page", "0")
                    .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("김트레이너"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공: 빈 결과 조회")
        void getAllTrainer_EmptyResult() throws Exception {
            // given
            Page<TrainerResponseDto> emptyPage = new PageImpl<>(
                List.of(), PageRequest.of(0, 10), 0);
            given(trainerService.getAllTrainer(anyLong(), any())).willReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/gyms/{gymId}/trainers", 1L)
                    .param("page", "0")
                    .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공: 트레이너 상세 조회")
        void getTrainerById_Success() throws Exception {
            // given
            given(trainerService.getTrainerById(anyLong(), anyLong()))
                .willReturn(trainerDetailResponseDto);

            // when & then
            mockMvc.perform(get("/gyms/{gymId}/trainers/{trainerId}", 1L, 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("김트레이너"))
                .andExpect(jsonPath("$.data.price").value(50000))
                .andExpect(jsonPath("$.data.trainerStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.createdAt").exists());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 존재하지 않는 트레이너")
        void getTrainerById_NotFound() throws Exception {
            // given
            given(trainerService.getTrainerById(anyLong(), anyLong()))
                .willThrow(new BaseException(ExceptionCode.TRAINER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/gyms/{gymId}/trainers/{trainerId}", 1L, 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 존재하지 않는 체육관")
        void getTrainerById_GymNotFound() throws Exception {
            // given
            given(trainerService.getTrainerById(anyLong(), anyLong()))
                .willThrow(new BaseException(ExceptionCode.GYM_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/gyms/{gymId}/trainers/{trainerId}", 999L, 1L))
                .andDo(print())
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공: 페이징 처리")
        void getAllTrainer_WithPaging() throws Exception {
            // given
            List<TrainerResponseDto> trainers = List.of(
                new TrainerResponseDto(1L, "트레이너1", 50000, "내용1", TrainerStatus.ACTIVE, "5년", Collections.emptyList()),
                new TrainerResponseDto(2L, "트레이너2", 60000, "내용2", TrainerStatus.ACTIVE, "6년", Collections.emptyList())
            );
            Page<TrainerResponseDto> trainerPage = new PageImpl<>(trainers, PageRequest.of(0, 2), 10);
            
            given(trainerService.getAllTrainer(anyLong(), any())).willReturn(trainerPage);

            // when & then
            mockMvc.perform(get("/gyms/{gymId}/trainers", 1L)
                    .param("page", "0")
                    .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(5))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.number").value(0));
        }
    }

    @Nested
    @DisplayName("트레이너 수정 API")
    class UpdateTrainerApi {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공: 트레이너 정보 수정")
        void updateTrainer_Success() throws Exception {
            // given
            TrainerUpdateRequestDto updateDto = new TrainerUpdateRequestDto(
                "수정된트레이너", 60000, "수정된 내용", "6년 경력", 
                TrainerStatus.ACTIVE, Collections.emptyList()
            );

            TrainerResponseDto updatedDto = new TrainerResponseDto(
                1L, "수정된트레이너", 60000, "수정된 내용", 
                TrainerStatus.ACTIVE, "6년 경력", Collections.emptyList()
            );

            given(trainerService.updateTrainer(anyLong(), anyLong(), anyLong(), 
                anyString(), anyInt(), anyString(), anyString(), any(), anyList()))
                .willReturn(updatedDto);

            // when & then
            mockMvc.perform(patch("/gyms/{gymId}/trainers/{trainerId}", 1L, 1L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된트레이너"))
                .andExpect(jsonPath("$.data.price").value(60000))
                .andExpect(jsonPath("$.data.trainerStatus").value("ACTIVE"));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공: 트레이너 휴가 상태 변경")
        void updateTrainer_StatusToHoliday() throws Exception {
            // given
            TrainerUpdateRequestDto updateDto = new TrainerUpdateRequestDto(
                "김트레이너", 50000, "전문 트레이너", "5년 경력", 
                TrainerStatus.HOLIDAY, Collections.emptyList()
            );

            TrainerResponseDto updatedDto = new TrainerResponseDto(
                1L, "김트레이너", 50000, "전문 트레이너", 
                TrainerStatus.HOLIDAY, "5년 경력", Collections.emptyList()
            );

            given(trainerService.updateTrainer(anyLong(), anyLong(), anyLong(), 
                anyString(), anyInt(), anyString(), anyString(), any(), anyList()))
                .willReturn(updatedDto);

            // when & then
            mockMvc.perform(patch("/gyms/{gymId}/trainers/{trainerId}", 1L, 1L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trainerStatus").value("HOLIDAY"));
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 권한 없음")
        void updateTrainer_Unauthorized() throws Exception {
            // given
            TrainerUpdateRequestDto updateDto = new TrainerUpdateRequestDto(
                "수정된트레이너", 60000, "수정된 내용", "6년 경력", 
                TrainerStatus.ACTIVE, Collections.emptyList()
            );

            given(trainerService.updateTrainer(anyLong(), anyLong(), anyLong(), 
                anyString(), anyInt(), anyString(), anyString(), any(), anyList()))
                .willThrow(new BaseException(ExceptionCode.NOT_GYM_OWNER));

            // when & then
            mockMvc.perform(patch("/gyms/{gymId}/trainers/{trainerId}", 1L, 1L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 잘못된 요청 데이터")
        void updateTrainer_InvalidRequest() throws Exception {
            // given
            TrainerUpdateRequestDto invalidDto = new TrainerUpdateRequestDto(
                "", -1000, "", "", TrainerStatus.ACTIVE, Collections.emptyList()
            );

            // when & then
            mockMvc.perform(patch("/gyms/{gymId}/trainers/{trainerId}", 1L, 1L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("트레이너 삭제 API")
    class DeleteTrainerApi {

        @Test
        @WithMockUser(username = "1")
        @DisplayName("성공: 트레이너 삭제")
        void deleteTrainer_Success() throws Exception {
            // given
            willDoNothing().given(trainerService)
                .deleteTrainer(anyLong(), anyLong(), anyLong());

            // when & then
            mockMvc.perform(delete("/gyms/{gymId}/trainers/{trainerId}", 1L, 1L)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 권한 없음")
        void deleteTrainer_Unauthorized() throws Exception {
            // given
            willThrow(new BaseException(ExceptionCode.NOT_GYM_OWNER))
                .given(trainerService).deleteTrainer(anyLong(), anyLong(), anyLong());

            // when & then
            mockMvc.perform(delete("/gyms/{gymId}/trainers/{trainerId}", 1L, 1L)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 존재하지 않는 트레이너")
        void deleteTrainer_NotFound() throws Exception {
            // given
            willThrow(new BaseException(ExceptionCode.TRAINER_NOT_FOUND))
                .given(trainerService).deleteTrainer(anyLong(), anyLong(), anyLong());

            // when & then
            mockMvc.perform(delete("/gyms/{gymId}/trainers/{trainerId}", 1L, 999L)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "1")
        @DisplayName("실패: 존재하지 않는 체육관")
        void deleteTrainer_GymNotFound() throws Exception {
            // given
            willThrow(new BaseException(ExceptionCode.GYM_NOT_FOUND))
                .given(trainerService).deleteTrainer(anyLong(), anyLong(), anyLong());

            // when & then
            mockMvc.perform(delete("/gyms/{gymId}/trainers/{trainerId}", 999L, 1L)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
        }
    }
}
*/
