package org.example.fitpass.chat.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
import org.example.fitpass.domain.chat.dto.ChatRoomResponseDto;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.enums.SenderType;
import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
import org.example.fitpass.domain.chat.service.ChatService;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.notify.entity.Notify;
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
@DisplayName("ChatService 통합 테스트")
class ChatIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

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

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User savedUser;
    private User savedTrainer;
    private Gym savedGym;

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

        savedTrainer = userRepository.save(new User(
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
    }

    @Test
    @DisplayName("채팅방 생성과 메시지 조회 통합 테스트")
    void createChatRoomAndRetrieveMessages_Integration() {
        // given & when - 채팅방 생성
        ChatRoomResponseDto chatRoomResponse = chatService.createOrGetChatRoom(savedUser.getId(), savedGym.getId());

        // then - 채팅방이 정상적으로 생성되었는지 확인
        assertThat(chatRoomResponse).isNotNull();
        assertThat(chatRoomResponse.userId()).isEqualTo(savedUser.getId());
        assertThat(chatRoomResponse.gymId()).isEqualTo(savedGym.getId());
        assertThat(chatRoomResponse.content()).isNull(); // 아직 메시지가 없음
        assertThat(chatRoomResponse.senderType()).isNull();

        // given - 채팅방에 메시지 추가
        ChatRoom savedChatRoom = chatRoomRepository.findById(chatRoomResponse.chatRoomId()).orElseThrow();
        ChatMessage message1 = chatMessageRepository.save(
            new ChatMessage(savedChatRoom, "안녕하세요! PT 문의드립니다.", SenderType.USER)
        );
        ChatMessage message2 = chatMessageRepository.save(
            new ChatMessage(savedChatRoom, "네, 어떤 도움이 필요하신가요?", SenderType.GYM)
        );

        // when - 메시지 조회
        List<ChatMessageResponseDto> messages = chatService.getMessageByChatRoomId(chatRoomResponse.chatRoomId());

        // then - 메시지가 시간순으로 정렬되어 조회되는지 확인
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).content()).isEqualTo("안녕하세요! PT 문의드립니다.");
        assertThat(messages.get(0).senderType()).isEqualTo(SenderType.USER);
        assertThat(messages.get(1).content()).isEqualTo("네, 어떤 도움이 필요하신가요?");
        assertThat(messages.get(1).senderType()).isEqualTo(SenderType.GYM);
    }

    @Test
    @DisplayName("사용자별 채팅방 목록 조회 - 일반 사용자")
    void getChatRoomsByUser_UserType_Integration() {
        // given - 채팅방과 메시지 생성
        ChatRoom chatRoom = chatRoomRepository.save(new ChatRoom(savedUser, savedGym));
        ChatMessage lastMessage = chatMessageRepository.save(
            new ChatMessage(chatRoom, "마지막 메시지입니다", SenderType.USER)
        );

        // when
        List<ChatRoomResponseDto> result = chatService.getChatRoomsByUser(savedUser.getId(), "USER");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(savedUser.getId());
        assertThat(result.get(0).gymId()).isEqualTo(savedGym.getId());
        assertThat(result.get(0).content()).isEqualTo("마지막 메시지입니다");
        assertThat(result.get(0).senderType()).isEqualTo(SenderType.USER);
    }

    @Test
    @DisplayName("트레이너별 채팅방 목록 조회 - 체육관 기준")
    void getChatRoomsByUser_TrainerType_Integration() {
        // given - 여러 사용자와의 채팅방 생성
        User anotherUser = userRepository.save(new User(
            "user2@test.com",
            null,
            "password123",
            "다른사용자",
            "010-5555-6666",
            28,
            "서울시 서초구",
            Gender.WOMAN,
            UserRole.USER,
            "LOCAL"
        ));

        ChatRoom chatRoom1 = chatRoomRepository.save(new ChatRoom(savedUser, savedGym));
        ChatRoom chatRoom2 = chatRoomRepository.save(new ChatRoom(anotherUser, savedGym));
        
        chatMessageRepository.save(new ChatMessage(chatRoom1, "첫 번째 채팅방 메시지", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(chatRoom2, "두 번째 채팅방 메시지", SenderType.GYM));

        // when
        List<ChatRoomResponseDto> result = chatService.getChatRoomsByUser(savedGym.getId(), "TRAINER");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(ChatRoomResponseDto::gymId))
            .allMatch(gymId -> gymId.equals(savedGym.getId()));
    }

    @Test
    @DisplayName("동일한 사용자와 체육관의 채팅방 중복 생성 방지")
    void createOrGetChatRoom_PreventDuplication_Integration() {
        // given & when - 첫 번째 채팅방 생성
        ChatRoomResponseDto firstCall = chatService.createOrGetChatRoom(savedUser.getId(), savedGym.getId());
        
        // when - 동일한 사용자와 체육관으로 두 번째 호출
        ChatRoomResponseDto secondCall = chatService.createOrGetChatRoom(savedUser.getId(), savedGym.getId());

        // then - 동일한 채팅방이 반환되는지 확인
        assertThat(firstCall.chatRoomId()).isEqualTo(secondCall.chatRoomId());
        
        // and - 데이터베이스에 채팅방이 하나만 존재하는지 확인
        List<ChatRoom> allChatRooms = chatRoomRepository.findAll();
        assertThat(allChatRooms).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 조회 시 예외 발생")
    void getChatRoomById_NotFound_ThrowsException() {
        // given
        Long nonExistentChatRoomId = 999L;

        // when & then
        assertThatThrownBy(() -> chatService.getChatRoomById(nonExistentChatRoomId))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining("채팅방을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 채팅방 목록 조회 시 예외 발생")
    void getChatRoomsByUser_UserNotFound_ThrowsException() {
        // given
        Long nonExistentUserId = 999L;

        // when & then
        assertThatThrownBy(() -> chatService.getChatRoomsByUser(nonExistentUserId, "USER"))
            .isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("존재하지 않는 체육관으로 채팅방 생성 시 예외 발생")
    void createOrGetChatRoom_GymNotFound_ThrowsException() {
        // given
        Long nonExistentGymId = 999L;

        // when & then
        assertThatThrownBy(() -> chatService.createOrGetChatRoom(savedUser.getId(), nonExistentGymId))
            .isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("메시지가 없는 채팅방의 메시지 조회")
    void getMessageByChatRoomId_EmptyMessages() {
        // given
        ChatRoom chatRoom = chatRoomRepository.save(new ChatRoom(savedUser, savedGym));

        // when
        List<ChatMessageResponseDto> messages = chatService.getMessageByChatRoomId(chatRoom.getId());

        // then
        assertThat(messages).isEmpty();
    }

    @Test
    @DisplayName("마지막 메시지가 없는 채팅방 목록 조회")
    void getChatRoomsByUser_NoLastMessage() {
        // given - 메시지가 없는 채팅방 생성
        chatRoomRepository.save(new ChatRoom(savedUser, savedGym));

        // when
        List<ChatRoomResponseDto> result = chatService.getChatRoomsByUser(savedUser.getId(), "USER");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isNull();
        assertThat(result.get(0).senderType()).isNull();
    }
}
