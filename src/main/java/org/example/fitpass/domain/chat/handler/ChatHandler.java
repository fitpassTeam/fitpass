package org.example.fitpass.domain.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.net.URI;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.enums.SenderType;
import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final ObjectMapper objectMapper;  // Jackson 라이브러리 필요

    private static final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    // Client 접속 시 호출되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessionMap.put(userId, session);
            log.info("사용자 {} 접속", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
        throws Exception {
        String payload = message.getPayload();

        try {
            //Json 여부 검사
            JSONObject json = new JSONObject(message.getPayload());

            Long senderId = json.getLong("senderId");
            Long receiverId = json.getLong("receiverId");
            String content = json.getString("message");
            String senderTypeStr = json.getString("senderType");
            SenderType senderType = SenderType.valueOf(senderTypeStr.toUpperCase());

            // 1. 사용자/트레이너 조회
            User user = userRepository.findById(
                    senderType == SenderType.USER ? senderId : receiverId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
            Trainer trainer = trainerRepository.findById(
                    senderType == SenderType.USER ? receiverId : senderId)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));

            // 2. 채팅방 조회/생성
            ChatRoom room = chatRoomRepository.findByUserAndTrainer(user, trainer)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.of(user, trainer)));

            // 3. 메시지 저장
            ChatMessage chatMessage = ChatMessage.of(room, senderId, content, senderType);
            chatMessage = chatMessageRepository.save(chatMessage);  // 저장 후 갱신

            //메세지 전송
            WebSocketSession receiverSession = sessionMap.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(content));
            } else {
                log.info("수신자 {} 오프라인", receiverId);
            }
        } catch (IllegalArgumentException e) { //JSON 내부에서 예상한 필드가 없거나 타입이 잘못됐을 때 발생 가능
            //JSON이 아닌 일반 메세지인 경우 전체 브로드캐스트
            log.warn("일반 메세지 수진 : {}", payload);
            for (WebSocketSession s : sessionMap.values()) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage("[Broadcast] " + payload));
                }
            }
        }

    }

    // Client 접속 해제 시 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessionMap.remove(userId);
            log.info("사용자 {} 접속 해제", userId);
        }
    }

    //WebSocket 세션에서 URI 쿼리 파라미터로 userId 추출
    //ex) ws://localhost:8080/chat?userId=1

    private Long extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }

        String query = uri.getQuery();
        if (query == null) {
            return null;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals("userId")) {
                return Long.parseLong(pair[1]);
            }
        }
        return null;
    }

}
