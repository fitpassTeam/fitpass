package org.example.fitpass.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.example.fitpass.domain.chat.controller.StompChatController;
import org.example.fitpass.domain.chat.dto.ChatMessageRequestDto;
import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("StompChatController 단위 테스트")
class StompChatControllerTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private StompChatController stompChatController;

    private User user;
    private Gym gym;
    private ChatRoom chatRoom;
    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        // 실제 User 객체 생성 (일반 사용자)
        user = new User(
            "user@example.com",
            null,
            "password123",
            "일반유저",
            "010-1234-5678",
            25,
            "서울시 강남구",
            Gender.MAN,
            UserRole.USER,
            "LOCAL"
        );
        setId(user, 1L);

        // 실제 User 객체 생성 (트레이너)
        User trainer = new User(
            "trainer@example.com",
            null,
            "password123",
            "트레이너",
            "010-9876-5432",
            30,
            "서울시 강남구",
            Gender.WOMAN,
            UserRole.OWNER,
            "LOCAL"
        );
        setId(trainer, 2L);

        // 실제 Gym 객체 생성
        gym = Gym.of(
            List.of("gym1.jpg", "gym2.jpg"),
            "테스트 체육관",
            "02-1234-5678",
            "테스트 체육관입니다",
            "서울시",
            "강남구",
            "테헤란로 332",
            LocalTime.of(6, 0),
            LocalTime.of(23, 0),
            "최고의 헬스장",
            trainer
        );
        setId(gym, 1L);

        // 실제 ChatRoom 객체 생성
        chatRoom = new ChatRoom(user, gym);
        setId(chatRoom, 1L);

        // 실제 ChatMessage 객체 생성
        chatMessage = new ChatMessage(chatRoom, "안녕하세요!", SenderType.USER);
        setId(chatMessage, 1L);
    }

    @Test
    @DisplayName("메시지 전송 - 사용자가 체육관에 메시지 전송")
    void sendMessage_UserToGym_Success() {
        // given
        ChatMessageRequestDto request = new ChatMessageRequestDto(
            1L, // senderId (user)
            1L, // receiverId (gym)
            "안녕하세요! PT 문의드립니다.",
            SenderType.USER
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(gymRepository.findById(1L)).willReturn(Optional.of(gym));
        given(chatRoomRepository.findByUserAndGym(1L, 1L)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

        // when
        ChatMessageResponseDto result = stompChatController.sendMessage(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("MESSAGE");
        assertThat(result.senderType()).isEqualTo(SenderType.USER);
        assertThat(result.content()).isEqualTo("안녕하세요!");

        then(userRepository).should().findById(1L);
        then(gymRepository).should().findById(1L);
        then(chatMessageRepository).should().save(any(ChatMessage.class));
        then(messagingTemplate).should().convertAndSend("/user/1/queue/messages", result);
    }

    @Test
    @DisplayName("메시지 전송 - 체육관이 사용자에게 메시지 전송")
    void sendMessage_GymToUser_Success() {
        // given
        ChatMessageRequestDto request = new ChatMessageRequestDto(
            1L, // senderId (gym)
            1L, // receiverId (user)
            "안녕하세요! 어떤 도움이 필요하신가요?",
            SenderType.GYM
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(gymRepository.findById(1L)).willReturn(Optional.of(gym));
        given(chatRoomRepository.findByUserAndGym(1L, 1L)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

        // when
        ChatMessageResponseDto result = stompChatController.sendMessage(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("MESSAGE");
        assertThat(result.senderType()).isEqualTo(SenderType.USER); // chatMessage의 senderType

        then(userRepository).should().findById(1L);
        then(gymRepository).should().findById(1L);
        then(chatMessageRepository).should().save(any(ChatMessage.class));
        then(messagingTemplate).should().convertAndSend("/user/1/queue/messages", result);
    }

    @Test
    @DisplayName("메시지 전송 - 채팅방이 존재하지 않아 새로 생성")
    void sendMessage_CreateNewChatRoom_Success() {
        // given
        ChatMessageRequestDto request = new ChatMessageRequestDto(
            1L, // senderId (user)
            1L, // receiverId (gym)
            "첫 메시지입니다",
            SenderType.USER
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(gymRepository.findById(1L)).willReturn(Optional.of(gym));
        given(chatRoomRepository.findByUserAndGym(1L, 1L)).willReturn(Optional.empty());
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(chatMessage);

        // when
        ChatMessageResponseDto result = stompChatController.sendMessage(request);

        // then
        assertThat(result).isNotNull();
        
        then(chatRoomRepository).should().findByUserAndGym(1L, 1L);
        then(chatRoomRepository).should().save(any(ChatRoom.class));
        then(chatMessageRepository).should().save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("메시지 전송 - 존재하지 않는 사용자")
    void sendMessage_UserNotFound_ThrowsException() {
        // given
        ChatMessageRequestDto request = new ChatMessageRequestDto(
            999L, // senderId (존재하지 않는 user)
            1L,   // receiverId (gym)
            "메시지",
            SenderType.USER
        );

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> stompChatController.sendMessage(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("메시지 처리 중 오류가 발생했습니다");

        then(userRepository).should().findById(999L);
    }

    @Test
    @DisplayName("메시지 전송 - 존재하지 않는 체육관")
    void sendMessage_GymNotFound_ThrowsException() {
        // given
        ChatMessageRequestDto request = new ChatMessageRequestDto(
            1L,   // senderId (user)
            999L, // receiverId (존재하지 않는 gym)
            "메시지",
            SenderType.USER
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(gymRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> stompChatController.sendMessage(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("메시지 처리 중 오류가 발생했습니다");

        then(userRepository).should().findById(1L);
        then(gymRepository).should().findById(999L);
    }

    @Test
    @DisplayName("사용자 채팅방 입장")
    void addUser_Success() {
        // given
        ChatMessageRequestDto request = new ChatMessageRequestDto(
            1L,
            null,
            null,
            SenderType.USER
        );

        given(headerAccessor.getSessionAttributes()).willReturn(new java.util.HashMap<>());

        // when
        ChatMessageResponseDto result = stompChatController.addUser(request, headerAccessor);

        // then
        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("JOIN");
        assertThat(result.senderId()).isEqualTo(1L);
        assertThat(result.senderType()).isEqualTo(SenderType.USER);
        assertThat(result.content()).isEqualTo("채팅방에 입장했습니다.");
    }

    @Test
    @DisplayName("사용자 채팅방 퇴장")
    void leaveUser_Success() {
        // given
        ChatMessageRequestDto request = new ChatMessageRequestDto(
            1L,
            null,
            null,
            SenderType.USER
        );

        // when
        ChatMessageResponseDto result = stompChatController.leaveUser(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("LEAVE");
        assertThat(result.senderId()).isEqualTo(1L);
        assertThat(result.senderType()).isEqualTo(SenderType.USER);
        assertThat(result.content()).isEqualTo("채팅방을 나갔습니다.");
    }

    @Test
    @DisplayName("트레이너 채팅방 입장")
    void addUser_Trainer_Success() {
        // given
        ChatMessageRequestDto request = new ChatMessageRequestDto(
            2L,
            null,
            null,
            SenderType.GYM
        );

        given(headerAccessor.getSessionAttributes()).willReturn(new java.util.HashMap<>());

        // when
        ChatMessageResponseDto result = stompChatController.addUser(request, headerAccessor);

        // then
        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("JOIN");
        assertThat(result.senderId()).isEqualTo(2L);
        assertThat(result.senderType()).isEqualTo(SenderType.GYM);
        assertThat(result.content()).isEqualTo("채팅방에 입장했습니다.");
    }

    /**
     * 리플렉션을 사용하여 엔티티의 ID를 설정하는 헬퍼 메서드
     */
    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("ID 설정 중 오류 발생", e);
        }
    }
}
