//package org.example.fitpass.chat.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//
//import java.lang.reflect.Field;
//import java.time.LocalTime;
//import java.util.List;
//import java.util.Optional;
//import org.example.fitpass.common.error.BaseException;
//import org.example.fitpass.common.error.ExceptionCode;
//import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
//import org.example.fitpass.domain.chat.dto.ChatRoomResponseDto;
//import org.example.fitpass.domain.chat.entity.ChatMessage;
//import org.example.fitpass.domain.chat.entity.ChatRoom;
//import org.example.fitpass.domain.chat.enums.SenderType;
//import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
//import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
//import org.example.fitpass.domain.chat.service.ChatService;
//import org.example.fitpass.domain.gym.entity.Gym;
//import org.example.fitpass.domain.gym.repository.GymRepository;
//import org.example.fitpass.domain.user.entity.User;
//import org.example.fitpass.domain.user.enums.Gender;
//import org.example.fitpass.domain.user.enums.UserRole;
//import org.example.fitpass.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("채팅 서비스 단위 테스트")
//class ChatServiceTest {
//
//    @Mock
//    private ChatMessageRepository chatMessageRepository;
//
//    @Mock
//    private ChatRoomRepository chatRoomRepository;
//
//    @Mock
//    private GymRepository gymRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private ChatService chatService;
//
//    private User user;
//    private User trainer;
//    private Gym gym;
//    private ChatRoom chatRoom;
//    private ChatMessage chatMessage;
//
//    @BeforeEach
//    void setUp() {
//        // 실제 User 객체 생성 (일반 사용자)
//        user = new User(
//            "user@example.com",
//            null,
//            "password123",
//            "일반유저",
//            "010-1234-5678",
//            25,
//            "서울시 강남구",
//            Gender.MAN,
//            UserRole.USER,
//            "LOCAL"
//        );
//        setId(user, 1L);
//
//        // 실제 User 객체 생성 (트레이너/체육관 사장)
//        trainer = new User(
//            "trainer@example.com",
//            null,
//            "password123",
//            "트레이너",
//            "010-9876-5432",
//            30,
//            "서울시 강남구",
//            Gender.MAN,
//            UserRole.OWNER,
//            "LOCAL"
//        );
//        setId(trainer, 2L);
//
//        // 실제 Gym 객체 생성 (정적 팩토리 메서드 사용)
//        gym = Gym.of(
//            List.of("gym1.jpg", "gym2.jpg"),
//            "테스트 체육관",
//            "02-1234-5678",
//            "테스트 체육관입니다",
//            "서울시",
//            "강남구",
//            "테헤란로 332",
//            LocalTime.of(6, 0),
//            LocalTime.of(23, 0),
//            "최고의 헬스장",
//            trainer
//        );
//        setId(gym, 1L);
//
//        // 실제 ChatRoom 객체 생성
//        chatRoom = new ChatRoom(user, gym);
//        setId(chatRoom, 1L);
//
//        // 실제 ChatMessage 객체 생성
//        chatMessage = new ChatMessage(chatRoom, "안녕하세요!", SenderType.USER);
//        setId(chatMessage, 1L);
//    }
//
//    @Test
//    @DisplayName("채팅방 메시지 조회 - 성공")
//    void getMessageByChatRoomId_Success() {
//        // given
//        List<ChatMessage> messages = List.of(chatMessage);
//        given(chatRoomRepository.getByIdOrThrow(anyLong())).willReturn(chatRoom);
//        given(chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(any(ChatRoom.class)))
//            .willReturn(messages);
//
//        // when
//        List<ChatMessageResponseDto> result = chatService.getMessageByChatRoomId(1L);
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).content()).isEqualTo("안녕하세요!");
//        assertThat(result.get(0).senderType()).isEqualTo(SenderType.USER);
//
//        then(chatRoomRepository).should().getByIdOrThrow(1L);
//        then(chatMessageRepository).should().findByChatRoomOrderByCreatedAtAsc(chatRoom);
//    }
//
//    @Test
//    @DisplayName("사용자별 채팅방 목록 조회 - 일반 사용자")
//    void getChatRoomsByUser_UserType_Success() {
//        // given
//        List<ChatRoom> chatRooms = List.of(chatRoom);
//        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//        given(chatRoomRepository.findByUser(any(User.class))).willReturn(chatRooms);
//        given(chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class)))
//            .willReturn(Optional.of(chatMessage));
//
//        // when
//        List<ChatRoomResponseDto> result = chatService.getChatRoomsByUser(1L, "USER");
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).userId()).isEqualTo(1L);
//        assertThat(result.get(0).gymId()).isEqualTo(1L);
//
//        then(userRepository).should().findByIdOrElseThrow(1L);
//        then(chatRoomRepository).should().findByUser(user);
//    }
//
//    @Test
//    @DisplayName("사용자별 채팅방 목록 조회 - 트레이너(체육관)")
//    void getChatRoomsByUser_TrainerType_Success() {
//        // given
//        List<ChatRoom> chatRooms = List.of(chatRoom);
//        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(gym);
//        given(chatRoomRepository.findByGym(any(Gym.class))).willReturn(chatRooms);
//        given(chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class)))
//            .willReturn(Optional.of(chatMessage));
//
//        // when - TRAINER 타입이므로 gymId를 전달
//        List<ChatRoomResponseDto> result = chatService.getChatRoomsByUser(gym.getId(), "TRAINER");
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).userId()).isEqualTo(1L);
//        assertThat(result.get(0).gymId()).isEqualTo(1L);
//
//        then(gymRepository).should().findByIdOrElseThrow(gym.getId());
//        then(chatRoomRepository).should().findByGym(gym);
//        then(userRepository).should(never()).findByIdOrElseThrow(anyLong());
//    }
//
//    @Test
//    @DisplayName("사용자별 채팅방 목록 조회 - 마지막 메시지가 없는 경우")
//    void getChatRoomsByUser_NoLastMessage_Success() {
//        // given
//        List<ChatRoom> chatRooms = List.of(chatRoom);
//        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//        given(chatRoomRepository.findByUser(any(User.class))).willReturn(chatRooms);
//        given(chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class)))
//            .willReturn(Optional.empty());
//
//        // when
//        List<ChatRoomResponseDto> result = chatService.getChatRoomsByUser(1L, "USER");
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).chatRoomId()).isEqualTo(1L);
//        assertThat(result.get(0).userId()).isEqualTo(1L);
//        assertThat(result.get(0).gymId()).isEqualTo(1L);
//        assertThat(result.get(0).content()).isNull();
//        assertThat(result.get(0).senderType()).isNull();
//    }
//
//    @Test
//    @DisplayName("채팅방 생성 또는 가져오기 - 기존 채팅방 존재")
//    void createOrGetChatRoom_ExistingRoom_Success() {
//        // given
//        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(gym);
//        given(chatRoomRepository.findByUserAndGym(any(User.class), any(Gym.class)))
//            .willReturn(Optional.of(chatRoom));
//        given(chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class)))
//            .willReturn(Optional.of(chatMessage));
//
//        // when
//        ChatRoomResponseDto result = chatService.createOrGetChatRoom(1L, 1L);
//
//        // then
//        assertThat(result.chatRoomId()).isEqualTo(1L);
//        assertThat(result.userId()).isEqualTo(1L);
//        assertThat(result.gymId()).isEqualTo(1L);
//
//        then(chatRoomRepository).should().findByUserAndGym(user, gym);
//        then(chatRoomRepository).should(never()).save(any(ChatRoom.class));
//    }
//
//    @Test
//    @DisplayName("채팅방 생성 또는 가져오기 - 새 채팅방 생성")
//    void createOrGetChatRoom_NewRoom_Success() {
//        // given
//        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//        given(gymRepository.findByIdOrElseThrow(anyLong())).willReturn(gym);
//        given(chatRoomRepository.findByUserAndGym(any(User.class), any(Gym.class)))
//            .willReturn(Optional.empty());
//        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);
//        given(chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class)))
//            .willReturn(Optional.empty());
//
//        // when
//        ChatRoomResponseDto result = chatService.createOrGetChatRoom(1L, 1L);
//
//        // then
//        assertThat(result.chatRoomId()).isEqualTo(1L);
//        assertThat(result.userId()).isEqualTo(1L);
//        assertThat(result.gymId()).isEqualTo(1L);
//
//        then(chatRoomRepository).should().findByUserAndGym(user, gym);
//        then(chatRoomRepository).should().save(any(ChatRoom.class));
//    }
//
//    @Test
//    @DisplayName("채팅방 상세 조회 - 성공")
//    void getChatRoomById_Success() {
//        // given
//        given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(chatRoom));
//
//        // when
//        ChatRoomResponseDto result = chatService.getChatRoomById(1L);
//
//        // then
//        assertThat(result.chatRoomId()).isEqualTo(1L);
//        assertThat(result.userId()).isEqualTo(1L);
//        assertThat(result.gymId()).isEqualTo(1L);
//
//        then(chatRoomRepository).should().findById(1L);
//    }
//
//    @Test
//    @DisplayName("채팅방 상세 조회 - 채팅방이 존재하지 않는 경우")
//    void getChatRoomById_NotFound_ThrowsException() {
//        // given
//        given(chatRoomRepository.findById(anyLong())).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> chatService.getChatRoomById(1L))
//            .isInstanceOf(BaseException.class)
//            .hasMessageContaining("채팅방을 찾을 수 없습니다");
//
//        then(chatRoomRepository).should().findById(1L);
//    }
//
//    @Test
//    @DisplayName("사용자별 채팅방 목록 조회 - 존재하지 않는 사용자")
//    void getChatRoomsByUser_UserNotFound_ThrowsException() {
//        // given
//        given(userRepository.findByIdOrElseThrow(anyLong()))
//            .willThrow(new BaseException(ExceptionCode.USER_NOT_FOUND));
//
//        // when & then
//        assertThatThrownBy(() -> chatService.getChatRoomsByUser(999L, "USER"))
//            .isInstanceOf(BaseException.class);
//
//        then(userRepository).should().findByIdOrElseThrow(999L);
//    }
//
//    @Test
//    @DisplayName("채팅방 생성 - 존재하지 않는 체육관")
//    void createOrGetChatRoom_GymNotFound_ThrowsException() {
//        // given
//        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//        given(gymRepository.findByIdOrElseThrow(anyLong()))
//            .willThrow(new BaseException(ExceptionCode.GYM_NOT_FOUND));
//
//        // when & then
//        assertThatThrownBy(() -> chatService.createOrGetChatRoom(1L, 999L))
//            .isInstanceOf(BaseException.class);
//
//        then(userRepository).should().findByIdOrElseThrow(1L);
//        then(gymRepository).should().findByIdOrElseThrow(999L);
//    }
//
//    @Test
//    @DisplayName("채팅방 생성 - 존재하지 않는 사용자")
//    void createOrGetChatRoom_UserNotFound_ThrowsException() {
//        // given
//        given(userRepository.findByIdOrElseThrow(anyLong()))
//            .willThrow(new BaseException(ExceptionCode.USER_NOT_FOUND));
//
//        // when & then
//        assertThatThrownBy(() -> chatService.createOrGetChatRoom(999L, 1L))
//            .isInstanceOf(BaseException.class);
//
//        then(userRepository).should().findByIdOrElseThrow(999L);
//        then(gymRepository).should(never()).findByIdOrElseThrow(anyLong());
//    }
//
//    @Test
//    @DisplayName("트레이너별 채팅방 목록 조회 - 존재하지 않는 체육관")
//    void getChatRoomsByUser_TrainerType_GymNotFound() {
//        // given
//        given(gymRepository.findByIdOrElseThrow(anyLong()))
//            .willThrow(new BaseException(ExceptionCode.GYM_NOT_FOUND));
//
//        // when & then
//        assertThatThrownBy(() -> chatService.getChatRoomsByUser(999L, "TRAINER"))
//            .isInstanceOf(BaseException.class);
//
//        then(gymRepository).should().findByIdOrElseThrow(999L);
//        then(userRepository).should(never()).findByIdOrElseThrow(anyLong());
//    }
//
//    @Test
//    @DisplayName("채팅방 목록 조회 - 여러 채팅방 정렬 확인")
//    void getChatRoomsByUser_MultipleChatRooms_OrderTest() {
//        // given
//        ChatRoom chatRoom2 = new ChatRoom(user, gym);
//        setId(chatRoom2, 2L);
//
//        ChatMessage recentMessage = new ChatMessage(chatRoom2, "최근 메시지", SenderType.GYM);
//        setId(recentMessage, 2L);
//
//        List<ChatRoom> chatRooms = List.of(chatRoom, chatRoom2);
//        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//        given(chatRoomRepository.findByUser(any(User.class))).willReturn(chatRooms);
//        given(chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom))
//            .willReturn(Optional.of(chatMessage));
//        given(chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom2))
//            .willReturn(Optional.of(recentMessage));
//
//        // when
//        List<ChatRoomResponseDto> result = chatService.getChatRoomsByUser(1L, "USER");
//
//        // then
//        assertThat(result).hasSize(2);
//        then(chatMessageRepository).should(times(2))
//            .findTopByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class));
//    }
//
//    @Test
//    @DisplayName("사용자별 채팅방 목록 조회 - 빈 채팅방 목록")
//    void getChatRoomsByUser_EmptyList() {
//        // given
//        given(userRepository.findByIdOrElseThrow(anyLong())).willReturn(user);
//        given(chatRoomRepository.findByUser(any(User.class))).willReturn(List.of());
//
//        // when
//        List<ChatRoomResponseDto> result = chatService.getChatRoomsByUser(1L, "USER");
//
//        // then
//        assertThat(result).isEmpty();
//        then(userRepository).should().findByIdOrElseThrow(1L);
//        then(chatRoomRepository).should().findByUser(user);
//        then(chatMessageRepository).should(never()).findTopByChatRoomOrderByCreatedAtDesc(any());
//    }
//
//    /**
//     * 리플렉션을 사용하여 엔티티의 ID를 설정하는 헬퍼 메서드
//     * @param entity ID를 설정할 엔티티
//     * @param id 설정할 ID 값
//     */
//    private void setId(Object entity, Long id) {
//        try {
//            Field idField = entity.getClass().getDeclaredField("id");
//            idField.setAccessible(true);
//            idField.set(entity, id);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException("ID 설정 중 오류 발생", e);
//        }
//    }
//}
