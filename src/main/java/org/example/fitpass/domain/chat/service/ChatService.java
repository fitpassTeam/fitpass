package org.example.fitpass.domain.chat.service;

import static org.example.fitpass.common.error.ExceptionCode.TRAINER_NOT_FOUND;
import static org.example.fitpass.common.error.ExceptionCode.USER_NOT_FOUND;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
import org.example.fitpass.domain.chat.dto.ChatRoomResponseDto;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TrainerRepository trainerRepository;
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
            Trainer trainer = trainerRepository.findByIdOrElseThrow(userId);
            chatRooms = chatRoomRepository.findByTrainer(trainer);
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
    public ChatRoomResponseDto createOrGetChatRoom(Long userId, Long trainerId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);

        ChatRoom chatRoom = chatRoomRepository.findByUserAndTrainer(user, trainer)
            .orElseGet(() -> chatRoomRepository.save(ChatRoom.of(user, trainer)));

        ChatMessage lastMessage = chatMessageRepository
            .findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
            .orElse(null);

        return ChatRoomResponseDto.from(chatRoom, lastMessage);
    }

}
