package org.example.fitpass.domain.chat.service;

import static org.example.fitpass.common.error.ExceptionCode.CHAT_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.entity.ChatRoom;
import org.example.fitpass.domain.chat.repository.ChatMessageRepository;
import org.example.fitpass.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public List<ChatMessage> getMessageByChatRoomId(Long chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(()-> new BaseException(CHAT_NOT_FOUND));
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
    }



}
