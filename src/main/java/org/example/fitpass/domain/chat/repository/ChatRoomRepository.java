package org.example.fitpass.domain.chat.repository;

import java.util.Optional;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByUserIdAndTrainerId(Long userId, Long trainerId);

}
