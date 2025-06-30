package org.example.fitpass.domain.chat.repository;

import static org.example.fitpass.common.error.ExceptionCode.CHAT_NOT_FOUND;
import static org.example.fitpass.common.error.ExceptionCode.USER_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    default ChatRoom getByIdOrThrow(Long chatRoomId) {
        return findById(chatRoomId).orElseThrow(() -> new BaseException(CHAT_NOT_FOUND));
    }

    // 성능 최적화를 위한 쿼리 메서드
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user.id = :userId AND cr.gym.id = :gymId")
    Optional<ChatRoom> findByUserAndGym(@Param("userId") Long userId, @Param("gymId") Long gymId);

    // 기존 메서드 (User, Gym 엔티티로 조회)
    Optional<ChatRoom> findByUserAndGym(User user, Gym gym);

    List<ChatRoom> findByUser(User user);

    List<ChatRoom> findByGym(Gym gym);

    // 성능 최적화를 위한 추가 메서드들
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user.id = :userId")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.gym.id = :gymId")
    List<ChatRoom> findByGymId(@Param("gymId") Long gymId);

}
