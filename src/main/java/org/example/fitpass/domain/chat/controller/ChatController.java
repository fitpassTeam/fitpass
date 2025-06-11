package org.example.fitpass.domain.chat.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.chat.entity.ChatMessage;
import org.example.fitpass.domain.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ws/chats")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ResponseMessage<List<ChatMessage>>> getMessages(
        @PathVariable("chatRoomId") Long chatRoomId) {
        List<ChatMessage> messages = chatService.getMessageByChatRoomId(chatRoomId);
        ResponseMessage<List<ChatMessage>> responseMessage =
            ResponseMessage.success(SuccessCode.GET_ALL_CHATTING, messages);
        return ResponseEntity.status(SuccessCode.GET_ALL_CHATTING.getHttpStatus())
            .body(responseMessage);
    }

}
