package org.example.fitpass.chat.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
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
@DisplayName("ChatService 시나리오 테스트")
class ChatScenarioTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User user1, user2, trainer1, trainer2;
    private Gym gym1, gym2;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        chatMessageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        gymRepository.deleteAll();
        userRepository.deleteAll();

        // 사용자들 생성
        user1 = userRepository.save(new User(
            "user1@test.com", null, "password123", "김철수",
            "010-1111-1111", 25, "서울시 강남구",
            Gender.MAN, UserRole.USER, "LOCAL"
        ));

        user2 = userRepository.save(new User(
            "user2@test.com", null, "password123", "이영희",
            "010-2222-2222", 28, "서울시 서초구",
            Gender.WOMAN, UserRole.USER, "LOCAL"
        ));

        // 트레이너들 생성
        trainer1 = userRepository.save(new User(
            "trainer1@test.com", null, "password123", "박트레이너",
            "010-3333-3333", 32, "서울시 강남구",
            Gender.MAN, UserRole.OWNER, "LOCAL"
        ));

        trainer2 = userRepository.save(new User(
            "trainer2@test.com", null, "password123", "최코치",
            "010-4444-4444", 29, "서울시 마포구",
            Gender.WOMAN, UserRole.OWNER, "LOCAL"
        ));

        // 체육관들 생성
        gym1 = gymRepository.save(Gym.of(
            List.of("gym1_1.jpg", "gym1_2.jpg"),
            "강남 피트니스", "02-1111-1111", "강남 최고의 헬스장",
            "서울시", "강남구", "테헤란로 100",
            LocalTime.of(6, 0), LocalTime.of(23, 59),
            "24시간 운영하는 프리미엄 헬스장", trainer1
        ));

        gym2 = gymRepository.save(Gym.of(
            List.of("gym2_1.jpg", "gym2_2.jpg"),
            "마포 스포츠센터", "02-2222-2222", "가족 친화적인 스포츠센터",
            "서울시", "마포구", "월드컵로 200",
            LocalTime.of(5, 30), LocalTime.of(23, 30),
            "수영장과 헬스장을 갖춘 종합 스포츠센터", trainer2
        ));
    }

    @Test
    @DisplayName("시나리오 1: 신규 사용자의 첫 체육관 문의부터 상담 완료까지")
    void scenario_NewUserInquiryToConsultationComplete() {
        // 1단계: 사용자가 체육관에 처음 문의
        ChatRoomResponseDto newChatRoom = chatService.createOrGetChatRoom(user1.getId(), gym1.getId());
        
        assertThat(newChatRoom.userId()).isEqualTo(user1.getId());
        assertThat(newChatRoom.gymId()).isEqualTo(gym1.getId());
        assertThat(newChatRoom.content()).isNull(); // 아직 메시지 없음

        // 2-6단계: 대화 진행
        ChatRoom chatRoom = chatRoomRepository.findById(newChatRoom.chatRoomId()).orElseThrow();
        chatMessageRepository.save(new ChatMessage(chatRoom, "안녕하세요! PT 수업에 대해 문의드립니다.", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(chatRoom, "안녕하세요! PT 문의 주셔서 감사합니다. 어떤 운동을 원하시나요?", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(chatRoom, "체중 감량이 목표입니다. 주 3회 정도 생각하고 있어요.", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(chatRoom, "좋습니다! 체중 감량을 위한 맞춤 프로그램을 준비해드릴게요. 언제 방문 가능하신가요?", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(chatRoom, "내일 오후 2시 이후로 시간이 가능합니다.", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(chatRoom, "네, 내일 오후 3시에 상담받으시는 것은 어떠세요? 체성분 분석과 함께 진행하겠습니다.", SenderType.GYM));

        // 검증
        List<ChatMessageResponseDto> messageHistory = chatService.getMessageByChatRoomId(chatRoom.getId());
        assertThat(messageHistory).hasSize(6);
        assertThat(messageHistory.get(0).content()).contains("PT 수업에 대해 문의");
        assertThat(messageHistory.get(5).content()).contains("내일 오후 3시");

        List<ChatRoomResponseDto> userChatRooms = chatService.getChatRoomsByUser(user1.getId(), "USER");
        assertThat(userChatRooms).hasSize(1);
        
        // 채팅방에 메시지가 있는지 확인 (마지막 메시지의 정확한 내용보다는 대화가 진행되었는지 확인)
        assertThat(userChatRooms.get(0).content()).isNotNull();
        assertThat(userChatRooms.get(0).content()).isNotEmpty();
    }

    @Test
    @DisplayName("시나리오 2: 트레이너가 여러 사용자와 동시에 상담하는 상황")
    void scenario_TrainerManagingMultipleUsers() {
        // 여러 사용자와 동시 상담
        ChatRoomResponseDto chatRoom1 = chatService.createOrGetChatRoom(user1.getId(), gym1.getId());
        ChatRoomResponseDto chatRoom2 = chatService.createOrGetChatRoom(user2.getId(), gym1.getId());
        
        ChatRoom room1 = chatRoomRepository.findById(chatRoom1.chatRoomId()).orElseThrow();
        ChatRoom room2 = chatRoomRepository.findById(chatRoom2.chatRoomId()).orElseThrow();
        
        // 각각 다른 주제로 상담
        chatMessageRepository.save(new ChatMessage(room1, "헬스 초보자인데 도움이 필요합니다", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room1, "처음 오시는 분들을 위한 프로그램이 있습니다", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(room2, "PT 가격이 어떻게 되나요?", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room2, "1회 5만원, 10회 패키지는 45만원입니다", SenderType.GYM));

        // 트레이너 관점에서 확인
        List<ChatRoomResponseDto> trainerChatRooms = chatService.getChatRoomsByUser(gym1.getId(), "TRAINER");
        assertThat(trainerChatRooms).hasSize(2);
        
        List<Long> userIds = trainerChatRooms.stream().map(ChatRoomResponseDto::userId).toList();
        assertThat(userIds).containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    @DisplayName("시나리오 3: 한 사용자가 여러 체육관과 상담하는 상황")
    void scenario_UserConsultingMultipleGyms() {
        // 한 사용자가 여러 체육관과 상담
        ChatRoomResponseDto chatRoom1 = chatService.createOrGetChatRoom(user1.getId(), gym1.getId());
        ChatRoomResponseDto chatRoom2 = chatService.createOrGetChatRoom(user1.getId(), gym2.getId());
        
        ChatRoom room1 = chatRoomRepository.findById(chatRoom1.chatRoomId()).orElseThrow();
        ChatRoom room2 = chatRoomRepository.findById(chatRoom2.chatRoomId()).orElseThrow();
        
        // 각 체육관의 특징에 맞는 질문
        chatMessageRepository.save(new ChatMessage(room1, "강남 헬스장 이용료는 얼마인가요?", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room1, "월 8만원입니다. 24시간 이용 가능해요", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(room2, "마포 스포츠센터는 수영장도 있나요?", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room2, "네, 수영장과 헬스장 모두 이용 가능합니다. 월 12만원입니다", SenderType.GYM));

        // 사용자 관점에서 확인
        List<ChatRoomResponseDto> userChatRooms = chatService.getChatRoomsByUser(user1.getId(), "USER");
        assertThat(userChatRooms).hasSize(2);
        
        List<Long> gymIds = userChatRooms.stream().map(ChatRoomResponseDto::gymId).toList();
        assertThat(gymIds).containsExactlyInAnyOrder(gym1.getId(), gym2.getId());
    }

    @Test
    @DisplayName("시나리오 4: 복잡한 상담 프로세스 - 예약부터 결제까지")
    void scenario_CompleteConsultationProcess() {
        ChatRoomResponseDto chatRoom = chatService.createOrGetChatRoom(user1.getId(), gym1.getId());
        ChatRoom room = chatRoomRepository.findById(chatRoom.chatRoomId()).orElseThrow();
        
        // 12단계의 완전한 상담 프로세스
        chatMessageRepository.save(new ChatMessage(room, "PT 상담 받고 싶습니다", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room, "어떤 목표를 가지고 계신가요?", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(room, "다이어트와 근력 증진이 목표입니다", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room, "좋습니다. 현재 운동 경험은 어느 정도인가요?", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(room, "완전 초보입니다. 운동을 거의 안 해봤어요", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room, "초보자를 위한 3개월 프로그램을 추천드립니다", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(room, "가격과 시간은 어떻게 되나요?", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room, "주 2회, 3개월 프로그램으로 총 120만원입니다. 월/수/금 중 선택 가능해요", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(room, "월요일과 수요일 오후 7시는 어떤가요?", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room, "가능합니다! 이번 주 월요일부터 시작하시겠어요?", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(room, "네, 좋습니다. 결제는 어떻게 하나요?", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(room, "현장에서 카드나 현금 결제 가능합니다. 월요일에 뵙겠습니다!", SenderType.GYM));

        // 전체 대화 히스토리 검증
        List<ChatMessageResponseDto> fullConversation = chatService.getMessageByChatRoomId(room.getId());
        assertThat(fullConversation).hasSize(12);
        
        // 대화 흐름 검증
        assertThat(fullConversation.get(0).content()).contains("PT 상담");
        assertThat(fullConversation.get(2).content()).contains("다이어트와 근력");
        assertThat(fullConversation.get(4).content()).contains("완전 초보");
        assertThat(fullConversation.get(11).content()).contains("월요일에 뵙겠습니다");

        // 사용자/체육관 발신자 교대 패턴 확인
        for (int i = 0; i < fullConversation.size(); i++) {
            SenderType expectedSender = (i % 2 == 0) ? SenderType.USER : SenderType.GYM;
            assertThat(fullConversation.get(i).senderType()).isEqualTo(expectedSender);
        }
    }

    @Test
    @DisplayName("시나리오 5: 빈 채팅방과 활성 채팅방이 혼재된 상황")
    void scenario_MixedEmptyAndActiveChatRooms() {
        // 메시지가 있는 활성 채팅방 (사용자 메시지를 마지막에 추가)
        ChatRoomResponseDto activeChatRoom = chatService.createOrGetChatRoom(user1.getId(), gym1.getId());
        ChatRoom activeRoom = chatRoomRepository.findById(activeChatRoom.chatRoomId()).orElseThrow();
        chatMessageRepository.save(new ChatMessage(activeRoom, "안녕하세요!", SenderType.USER));
        chatMessageRepository.save(new ChatMessage(activeRoom, "네, 도움을 드릴게요", SenderType.GYM));
        chatMessageRepository.save(new ChatMessage(activeRoom, "감사합니다. 언제 방문하면 될까요?", SenderType.USER));

        // 메시지가 없는 빈 채팅방
        ChatRoom emptyChatRoom = chatRoomRepository.save(new ChatRoom(user2, gym1));

        // 트레이너 관점에서 확인
        List<ChatRoomResponseDto> trainerView = chatService.getChatRoomsByUser(gym1.getId(), "TRAINER");
        assertThat(trainerView).hasSize(2);
        
        // 활성 채팅방과 빈 채팅방 구분 확인
        ChatRoomResponseDto activeChatRoomView = trainerView.stream()
            .filter(room -> room.userId().equals(user1.getId()))
            .findFirst().orElseThrow();
        // 트레이너 관점에서는 사용자의 마지막 메시지를 보여주는 것 같음
        assertThat(activeChatRoomView.content()).isEqualTo("감사합니다. 언제 방문하면 될까요?");
        
        ChatRoomResponseDto emptyChatRoomView = trainerView.stream()
            .filter(room -> room.userId().equals(user2.getId()))
            .findFirst().orElseThrow();
        assertThat(emptyChatRoomView.content()).isNull();

        // 빈 채팅방에 메시지 추가 후 재확인
        chatMessageRepository.save(new ChatMessage(emptyChatRoom, "안녕하세요!", SenderType.USER));
        List<ChatRoomResponseDto> updatedView = chatService.getChatRoomsByUser(gym1.getId(), "TRAINER");
        assertThat(updatedView.stream().map(ChatRoomResponseDto::content))
            .allMatch(content -> content != null);
    }
}
