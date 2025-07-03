package org.example.fitpass.domain.chat.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
import org.example.fitpass.domain.chat.dto.ChatRoomResponseDto;
import org.example.fitpass.domain.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ws/chatRooms")
public class ChatController {

    private final ChatService chatService;

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<ResponseMessage<ChatRoomResponseDto>> createOrGetChatRoom(
        @RequestParam("userId") Long userId,
        @RequestParam("gymId") Long gymId
    ) {
        ChatRoomResponseDto chatRoom = chatService.createOrGetChatRoom(userId, gymId);
        return ResponseEntity.status((SuccessCode.CREATE_CHATROOM.getHttpStatus()))
            .body(ResponseMessage.success(SuccessCode.CREATE_CHATROOM, chatRoom));
    }

    // 채팅방 목록 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<List<ChatRoomResponseDto>>> getChatRoomsByUser(
        @RequestParam Long userId,
        @RequestParam String userType
    ) {
        List<ChatRoomResponseDto> dto = chatService.getChatRoomsByUser(userId, userType);
        return ResponseEntity.status(SuccessCode.GET_ALL_CHATROOM.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ALL_CHATROOM, dto));
    }

    // 채팅 내역 조회
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ResponseMessage<List<ChatMessageResponseDto>>> getMessages(
        @PathVariable("chatRoomId") Long chatRoomId
    ) {
        List<ChatMessageResponseDto> messages = chatService.getMessageByChatRoomId(chatRoomId);
        return ResponseEntity.status(SuccessCode.GET_ALL_CHATTING.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ALL_CHATTING, messages));
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ResponseMessage<ChatRoomResponseDto>> getChatRoom(
        @PathVariable("chatRoomId") Long chatRoomId
    ) {
        ChatRoomResponseDto chatRoom = chatService.getChatRoomById(chatRoomId);
        return ResponseEntity.ok(ResponseMessage.success(SuccessCode.GET_CHATROOM, chatRoom));
    }

}
