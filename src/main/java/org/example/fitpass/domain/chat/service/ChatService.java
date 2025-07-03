    package org.example.fitpass.domain.chat.service;

    import java.util.List;
    import java.util.stream.Collectors;
    import lombok.RequiredArgsConstructor;
    import org.example.fitpass.common.error.BaseException;
    import org.example.fitpass.common.error.ExceptionCode;
    import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
    import org.example.fitpass.domain.chat.dto.ChatRoomResponseDto;
    import org.example.fitpass.domain.chat.entity.ChatMessage;
    import org.example.fitpass.domain.chat.entity.ChatRoom;
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

        public List<ChatMessageResponseDto> getMessageByChatRoomId(Long chatRoomId) {
            ChatRoom chatRoom = chatRoomRepository.getByIdOrThrow(chatRoomId);
            return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom).stream()
                .map(ChatMessageResponseDto::from)
                .toList();
        }

        // 사용자별 채팅방 목록 조회
        public List<ChatRoomResponseDto> getChatRoomsByUser(Long userId, String userType) {
            List<ChatRoom> chatRooms;

            if ("USER".equalsIgnoreCase(userType)) {
                User user = userRepository.findByIdOrElseThrow(userId);
                chatRooms = chatRoomRepository.findByUser(user);
            } else {
                Gym gym = gymRepository.findByIdOrElseThrow(userId);
                chatRooms = chatRoomRepository.findByGym(gym);
            }

            return chatRooms.stream()
                .map(chatRoom -> {
                    ChatMessage lastMessage = chatMessageRepository
                        .findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                        .orElse(null); // 마지막 메시지가 없을 수도 있으므로 null 허용
                    return ChatRoomResponseDto.from(chatRoom, lastMessage);
                })
                .collect(Collectors.toList());

        }

        @Transactional
        public ChatRoomResponseDto createOrGetChatRoom(Long userId, Long gymId) {
            User user = userRepository.findByIdOrElseThrow(userId);
            Gym gym = gymRepository.findByIdOrElseThrow(gymId);

            ChatRoom chatRoom = chatRoomRepository.findByUserAndGym(user, gym)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.of(user, gym)));

            ChatMessage lastMessage = chatMessageRepository
                .findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .orElse(null);

            return ChatRoomResponseDto.from(chatRoom, lastMessage);
        }

        public ChatRoomResponseDto getChatRoomById(Long chatRoomId) {
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(ExceptionCode.CHATROOM_NOT_FOUND));
            return new ChatRoomResponseDto(
                chatRoom.getId(),
                chatRoom.getUser().getId(), // userId
                chatRoom.getGym().getId(),  // gymId
                null,
                null
            );
        }

    }
