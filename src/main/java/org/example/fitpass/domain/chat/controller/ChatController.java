package org.example.fitpass.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
import org.example.fitpass.domain.chat.dto.ChatRoomResponseDto;
import org.example.fitpass.domain.chat.entity.ChatMessage;
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
@Tag(name = "Chat API", description = "채팅 관련 API - 트레이너와 회원 간 실시간 채팅")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "채팅방 생성",
        description = "트레이너와 회원 간의 새로운 채팅방을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "채팅방 생성 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 체육관을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 채팅방"),
        @ApiResponse(responseCode = "401", description = "인증이 필요함")
    })
    @Parameter(name = "userId", description = "회원 ID", example = "1")
    @Parameter(name = "gymId", description = "체육관 ID", example = "1")
    @PostMapping
    public ResponseEntity<ResponseMessage<ChatRoomResponseDto>> createOrGetChatRoom(
        @RequestParam("userId") Long userId,
        @RequestParam("gymId") Long gymId) {
        ChatRoomResponseDto chatRoom = chatService.createOrGetChatRoom(userId, gymId);
        return ResponseEntity.status((SuccessCode.CREATE_CHATROOM.getHttpStatus()))
            .body(ResponseMessage.success(SuccessCode.CREATE_CHATROOM, chatRoom));
    }

    @Operation(summary = "채팅방 목록 조회",
        description = "사용자가 참여 중인 모든 채팅방 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증이 필요함")
    })
    @Parameter(name = "userId", description = "사용자 ID", example = "1")
    @Parameter(name = "userType", description = "사용자 타입 (USER 또는 TRAINER)", example = "USER")
    @GetMapping
    public ResponseEntity<ResponseMessage<List<ChatRoomResponseDto>>> getChatRoomsByUser(
        @RequestParam Long userId,
        @RequestParam String userType) {
        List<ChatRoomResponseDto> dto = chatService.getChatRoomsByUser(userId, userType);
        return ResponseEntity.status(SuccessCode.GET_ALL_CHATROOM.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ALL_CHATROOM, dto));
    }

    @Operation(summary = "채팅 내역 조회",
        description = "특정 채팅방의 모든 메시지 내역을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅 내역 조회 성공"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "채팅방 접근 권한이 없음"),
        @ApiResponse(responseCode = "401", description = "인증이 필요함")
    })
    @Parameter(name = "chatRoomId", description = "채팅방 ID", example = "1")
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ResponseMessage<List<ChatMessageResponseDto>>> getMessages(
        @PathVariable("chatRoomId") Long chatRoomId) {
        List<ChatMessageResponseDto> messages = chatService.getMessageByChatRoomId(chatRoomId);
        return ResponseEntity.status(SuccessCode.GET_ALL_CHATTING.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_ALL_CHATTING, messages));
    }

    @Operation(summary = "채팅방 상세 조회",
        description = "특정 채팅방의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 상세 조회 성공"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "채팅방 접근 권한이 없음"),
        @ApiResponse(responseCode = "401", description = "인증이 필요함")
    })
    @Parameter(name = "chatRoomId", description = "채팅방 ID", example = "1")
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ResponseMessage<ChatRoomResponseDto>> getChatRoom(
        @PathVariable("chatRoomId") Long chatRoomId) {
        ChatRoomResponseDto chatRoom = chatService.getChatRoomById(chatRoomId);
        return ResponseEntity.ok(ResponseMessage.success(SuccessCode.GET_CHATROOM, chatRoom));
    }

}
