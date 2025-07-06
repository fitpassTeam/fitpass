package org.example.fitpass.domain.chat.repository;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.enums.SenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 🔸 안 읽은 메시지 수 카운트
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom = :chatRoom AND m.senderType != :receiverType AND m.isRead = false")
    Long countUnreadMessages(@Param("chatRoom") ChatRoom chatRoom, @Param("receiverType") SenderType receiverType);

    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomAndSenderTypeNotAndIsReadFalse(ChatRoom chatRoom, SenderType receiverType);

    // Repository에 추가할 메서드들
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom IN :chatRooms ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLastMessagesByChatRooms(@Param("chatRooms") List<ChatRoom> chatRooms);

    @Query("SELECT cm.chatRoom.id, COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom IN :chatRooms AND cm.senderType != :receiverType AND cm.isRead = false GROUP BY cm.chatRoom.id")
    List<Object[]> countUnreadMessagesByChatRooms(@Param("chatRooms") List<ChatRoom> chatRooms, @Param("receiverType") SenderType receiverType);

}
