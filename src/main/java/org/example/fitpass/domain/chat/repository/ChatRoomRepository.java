package org.example.fitpass.domain.chat.repository;

import static org.example.fitpass.common.error.ExceptionCode.CHAT_NOT_FOUND;

import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    default ChatRoom getByIdOrThrow(Long chatRoomId) {
        return findById(chatRoomId).orElseThrow(() -> new BaseException(CHAT_NOT_FOUND));
    }

    Optional<ChatRoom> findByUserIdAndTrainerId(Long userId, Long trainerId);

}
