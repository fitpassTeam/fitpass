package org.example.fitpass.chat.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.time.LocalDateTime;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.chat.dto.ChatMessageResponseDto;
import org.example.fitpass.domain.chat.dto.ChatRoomResponseDto;
import org.example.fitpass.domain.chat.enums.SenderType;
import org.example.fitpass.domain.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ChatController 단위 테스트")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    private ChatRoomResponseDto chatRoomResponse;
    private ChatMessageResponseDto chatMessageResponse;

    @BeforeEach
    void setUp() {
        chatRoomResponse = new ChatRoomResponseDto(
            1L, 1L, 1L, "안녕하세요!", SenderType.USER, 0
        );

        chatMessageResponse = new ChatMessageResponseDto(
            "CHAT", 1L, 1L, SenderType.USER, "안녕하세요!", LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("채팅방 생성 - 성공")
    void createOrGetChatRoom_Success() throws Exception {
        // given
        given(chatService.createOrGetChatRoom(anyLong(), anyLong()))
            .willReturn(chatRoomResponse);

        // when & then
        mockMvc.perform(post("/ws/chatRooms")
                .param("userId", "1")
                .param("gymId", "1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.message").value("채팅방이 생성되었습니다."))
            .andExpect(jsonPath("$.data.chatRoomId").value(1L));
    }

    @Test
    @DisplayName("채팅방 생성 - 존재하지 않는 사용자")
    void createOrGetChatRoom_UserNotFound() throws Exception {
        // given
        given(chatService.createOrGetChatRoom(anyLong(), anyLong()))
            .willThrow(new BaseException(ExceptionCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/ws/chatRooms")
                .param("userId", "999")
                .param("gymId", "1"))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("채팅방 목록 조회 - 성공")
    void getChatRoomsByUser_Success() throws Exception {
        // given
        List<ChatRoomResponseDto> chatRooms = List.of(chatRoomResponse);
        given(chatService.getChatRoomsByUser(anyLong(), anyString()))
            .willReturn(chatRooms);

        // when & then
        mockMvc.perform(get("/ws/chatRooms")
                .param("userId", "1")
                .param("userType", "USER"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("채팅 내역 조회 - 존재하지 않는 채팅방")
    void getMessages_ChatRoomNotFound() throws Exception {
        // given
        given(chatService.getMessageByChatRoomId(anyLong()))
            .willThrow(new BaseException(ExceptionCode.CHATROOM_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/ws/chatRooms/{chatRoomId}/messages", 999L))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("채팅방 상세 조회 - 성공")
    void getChatRoom_Success() throws Exception {
        // given
        given(chatService.getChatRoomById(anyLong()))
            .willReturn(chatRoomResponse);

        // when & then
        mockMvc.perform(get("/ws/chatRooms/{chatRoomId}", 1L))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("채팅방 상세 조회 - 존재하지 않는 채팅방")
    void getChatRoom_NotFound() throws Exception {
        // given
        given(chatService.getChatRoomById(anyLong()))
            .willThrow(new BaseException(ExceptionCode.CHATROOM_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/ws/chatRooms/{chatRoomId}", 999L))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }
}
