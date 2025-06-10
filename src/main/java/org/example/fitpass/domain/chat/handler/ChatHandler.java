package org.example.fitpass.domain.chat.handler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
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

    private static final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    // Client 접속 시 호출되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
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

        try{
            //Json 여부 검사
            JSONObject json = new JSONObject(message.getPayload());

            Long senderId = json.getLong("senderId");
            Long receiverId = json.getLong("receiverId");
            String content = json.getString("message");

            //채팅방 조회 또는 생성
            ChatRoom room = chatRoomRepository.findByUserIdAndTrainerId(senderId, receiverId).orElseGet(
                () -> chatRoomRepository.save(ChatRoom.of(senderId, receiverId)));

            //메세지 저장
            ChatMessage chatMessage = ChatMessage.of(room, senderId, content);
            chatMessageRepository.save(chatMessage);

            //메세지 전송
            WebSocketSession receiverSession = sessionMap.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(content));
            }
        } catch (Exception e) {
            //JSON이 아닌 일반 메세지인 경우 전체 브로드캐스트
            log.warn("일반 메세지 수진 : {}", payload);
            for(WebSocketSession s : sessionMap.values()){
                if(s.isOpen()){
                    s.sendMessage(new TextMessage("[Broadcast] " + payload));
                }
            }
        }

    }

    // Client 접속 해제 시 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
        throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessionMap.put(userId, session);
            log.info("사용자 {} 접속", userId);
        }
    }

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
