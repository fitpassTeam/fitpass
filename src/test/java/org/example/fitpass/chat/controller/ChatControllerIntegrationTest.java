package org.example.fitpass.chat.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.enums.SenderType;
import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("ChatController 통합 테스트")
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User savedUser;
    private Gym savedGym;
    private ChatRoom savedChatRoom;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        chatMessageRepository.deleteAll();
        chatRoomRepository.deleteAll();
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

        User savedTrainer = userRepository.save(new User(
            "trainer@test.com",
            null,
            "password123",
            "트레이너",
            "010-9876-5432",
            30,
            "서울시 강남구",
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
            LocalTime.of(23, 0),
            "깨끗하고 시설이 좋은 헬스장",
            savedTrainer
        ));

        savedChatRoom = chatRoomRepository.save(new ChatRoom(savedUser, savedGym));
    }

    @Test
    @DisplayName("채팅방 생성 API 통합 테스트 - 성공")
    void createOrGetChatRoom_Integration_Success() throws Exception {
        mockMvc.perform(post("/ws/chatRooms")
                .param("userId", savedUser.getId().toString())
                .param("gymId", savedGym.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("채팅방이 생성되었습니다."))
            .andExpect(jsonPath("$.data.userId").value(savedUser.getId()))
            .andExpect(jsonPath("$.data.gymId").value(savedGym.getId()))
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.senderType").isEmpty());
    }

    @Test
    @DisplayName("채팅방 목록 조회 API 통합 테스트 - 트레이너")
    void getChatRoomsByUser_Integration_TrainerType() throws Exception {
        // given - 채팅방에 메시지 추가
        chatMessageRepository.save(new ChatMessage(savedChatRoom, "PT 문의드립니다", SenderType.USER));

        // when & then
        mockMvc.perform(get("/ws/chatRooms")
                .param("userId", savedGym.getId().toString())
                .param("userType", "TRAINER")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].userId").value(savedUser.getId()))
            .andExpect(jsonPath("$.data[0].gymId").value(savedGym.getId()))
            .andExpect(jsonPath("$.data[0].content").value("PT 문의드립니다"))
            .andExpect(jsonPath("$.data[0].senderType").value("USER"));
    }

    @Test
    @DisplayName("채팅 내역 조회 API 통합 테스트")
    void getMessages_Integration_Success() throws Exception {
        // given
        chatMessageRepository.save(new ChatMessage(savedChatRoom, "첫 번째 메시지", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(savedChatRoom, "두 번째 메시지", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(savedChatRoom, "세 번째 메시지", SenderType.USER));

        // when & then
        mockMvc.perform(get("/ws/chatRooms/{chatRoomId}/messages", savedChatRoom.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("채팅 내역이 조회되었습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].content").value("첫 번째 메시지"))
            .andExpect(jsonPath("$.data[0].senderType").value("USER"))
            .andExpect(jsonPath("$.data[1].content").value("두 번째 메시지"))
            .andExpect(jsonPath("$.data[1].senderType").value("GYM"))
            .andExpect(jsonPath("$.data[2].content").value("세 번째 메시지"))
            .andExpect(jsonPath("$.data[2].senderType").value("USER"));
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 조회 시 404 에러")
    void getChatRoom_NotFound_Integration() throws Exception {
        Long nonExistentChatRoomId = 999L;

        mockMvc.perform(get("/ws/chatRooms/{chatRoomId}", nonExistentChatRoomId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 채팅방 생성 시 404 에러")
    void createOrGetChatRoom_UserNotFound_Integration() throws Exception {
        Long nonExistentUserId = 999L;

        mockMvc.perform(post("/ws/chatRooms")
                .param("userId", nonExistentUserId.toString())
                .param("gymId", savedGym.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 체육관으로 채팅방 생성 시 404 에러")
    void createOrGetChatRoom_GymNotFound_Integration() throws Exception {
        Long nonExistentGymId = 999L;

        mockMvc.perform(post("/ws/chatRooms")
                .param("userId", savedUser.getId().toString())
                .param("gymId", nonExistentGymId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

}
