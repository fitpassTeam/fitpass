    package org.example.fitpass.domain.chat.service;

    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;
    import lombok.RequiredArgsConstructor;
    import org.example.fitpass.common.error.BaseException;
    import org.example.fitpass.common.error.ExceptionCode;
    import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
    import org.example.fitpass.domain.chat.dto.ChatRoomResponseDto;
    import org.example.fitpass.domain.chat.entity.ChatMessage;
    import org.example.fitpass.domain.chat.entity.ChatRoom;
    import org.example.fitpass.domain.chat.enums.SenderType;
    import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
    import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
    import org.example.fitpass.domain.gym.entity.Gym;
    import org.example.fitpass.domain.gym.repository.GymRepository;
    import org.example.fitpass.domain.user.entity.User;
    import org.example.fitpass.domain.user.repository.UserRepository;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    @Service
    @RequiredArgsConstructor
    public class ChatService {

        private final ChatMessageRepository chatMessageRepository;
        private final ChatRoomRepository chatRoomRepository;
        private final GymRepository gymRepository;
        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public List<ChatMessageResponseDto> getMessageByChatRoomId(Long chatRoomId) {
            ChatRoom chatRoom = chatRoomRepository.getByIdOrThrow(chatRoomId);
            return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom).stream()
                .map(ChatMessageResponseDto::from)
                .toList();
        }

        private SenderType determineReceiverType(String userType) {
            if ("USER".equalsIgnoreCase(userType) || "PENDING_OWNER".equalsIgnoreCase(userType)) {
                return SenderType.USER;
            } else {
                return SenderType.GYM; // OWNER나 기타 타입 처리
            }
        }

        // 사용자별 채팅방 목록 조회 - N+1 문제 완전 해결
        @Transactional(readOnly = true)
        public List<ChatRoomResponseDto> getChatRoomsByUser(Long userId, String userType) {
            List<ChatRoom> chatRooms;

            if ("USER".equalsIgnoreCase(userType) || "PENDING_OWNER".equalsIgnoreCase(userType)) {
                User user = userRepository.findByIdOrElseThrow(userId);
                chatRooms = chatRoomRepository.findByUser(user);
            } else {
                Gym gym = gymRepository.findByIdOrElseThrow(userId);
                chatRooms = chatRoomRepository.findByGym(gym);
            }

            // 빈 리스트면 조기 반환
            if (chatRooms.isEmpty()) {
                return List.of();
            }

            SenderType receiverType = determineReceiverType(userType);

            // 🚀 배치 쿼리로 N+1 문제 완전 해결
            // 1. 모든 마지막 메시지를 한 번에 조회
            List<ChatMessage> lastMessages = chatMessageRepository.findLastMessagesByChatRooms(chatRooms);
            
            // 2. 모든 읽지 않은 메시지 수를 한 번에 조회
            List<Object[]> unreadCounts = chatMessageRepository.countUnreadMessagesByChatRooms(chatRooms, receiverType);

            // 3. Map으로 변환하여 O(1) 접근 가능하게 만듦
            Map<Long, ChatMessage> lastMessageMap = lastMessages.stream()
                .collect(Collectors.toMap(
                    message -> message.getChatRoom().getId(),
                    message -> message,
                    (existing, replacement) -> existing // 중복 시 기존 것 유지
                ));
                
            Map<Long, Long> unreadCountMap = unreadCounts.stream()
                .collect(Collectors.toMap(
                    arr -> (Long) arr[0], // chatRoomId
                    arr -> (Long) arr[1]  // count
                ));

            // 4. 각 채팅방에 대해 Map에서 데이터 조회 (추가 DB 쿼리 없음)
            return chatRooms.stream()
                .map(chatRoom -> {
                    ChatMessage lastMessage = lastMessageMap.get(chatRoom.getId());
                    Long unreadCount = unreadCountMap.getOrDefault(chatRoom.getId(), 0L);
                    return ChatRoomResponseDto.from(chatRoom, lastMessage, unreadCount.intValue());
                })
                .collect(Collectors.toList());
        }

        @Transactional
        public ChatRoomResponseDto createOrGetChatRoom(Long userId, Long gymId) {
            User user = userRepository.findByIdOrElseThrow(userId);
            Gym gym = gymRepository.findByIdOrElseThrow(gymId);

            ChatRoom chatRoom = chatRoomRepository.findByUserAndGym(user, gym)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.of(user, gym)));

            // 🔥 최적화: 새로 생성된 채팅방이면 추가 쿼리 생략
            if (chatRoom.getId() != null && chatRoomRepository.findById(chatRoom.getId()).isEmpty()) {
                // 새로 생성된 채팅방은 메시지가 없음
                return ChatRoomResponseDto.from(chatRoom, null, 0);
            }

            ChatMessage lastMessage = chatMessageRepository
                .findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .orElse(null);

            SenderType receiverType = SenderType.USER;
            Long unreadCount = chatMessageRepository.countUnreadMessages(chatRoom, receiverType);

            return ChatRoomResponseDto.from(chatRoom, lastMessage, unreadCount.intValue());
        }

        @Transactional(readOnly = true)
        public ChatRoomResponseDto getChatRoomById(Long chatRoomId) {
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(ExceptionCode.CHATROOM_NOT_FOUND));
            return new ChatRoomResponseDto(
                chatRoom.getId(),
                chatRoom.getUser().getId(), // userId
                chatRoom.getGym().getId(),  // gymId
                null,
                null,
                0
            );
        }

        @Transactional
        public void markMessagesAsRead(Long chatRoomId, String receiverTypeStr) {
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(ExceptionCode.CHATROOM_NOT_FOUND));

            SenderType receiverType = SenderType.valueOf(receiverTypeStr.toUpperCase());
            
            // 🔥 최적화: 배치 업데이트 쿼리 사용 (가능하다면)
            List<ChatMessage> unreadMessages = chatMessageRepository
                .findByChatRoomAndSenderTypeNotAndIsReadFalse(chatRoom, receiverType);

            if (!unreadMessages.isEmpty()) {
                unreadMessages.forEach(ChatMessage::markAsRead);
                chatMessageRepository.saveAll(unreadMessages);
            }
        }

    }
