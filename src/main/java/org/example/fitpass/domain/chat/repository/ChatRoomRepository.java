package org.example.fitpass.domain.chat.repository;

import static org.example.fitpass.common.error.ExceptionCode.CHAT_NOT_FOUND;
import static org.example.fitpass.common.error.ExceptionCode.USER_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    default ChatRoom getByIdOrThrow(Long chatRoomId) {
        return findById(chatRoomId).orElseThrow(() -> new BaseException(CHAT_NOT_FOUND));
    }

    Optional<ChatRoom> findByUserAndTrainer(User user, Trainer trainer);

    List<ChatRoom> findByUser(User user);

    List<ChatRoom> findByTrainer(Trainer trainer);

}
