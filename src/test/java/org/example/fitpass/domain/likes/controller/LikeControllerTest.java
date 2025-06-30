package org.example.fitpass.domain.likes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.likes.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LikeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LikeService likeService;

    @InjectMocks
    private LikeController likeController;

    private ObjectMapper objectMapper;
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(likeController).build();
        objectMapper = new ObjectMapper();
        
        mockUserDetails = mock(CustomUserDetails.class);
        when(mockUserDetails.getId()).thenReturn(1L);
        when(mockUserDetails.getUsername()).thenReturn("test@email.com");
    }

    @Test
    @DisplayName("헬스장 좋아요 - 성공")
    void postGymLike_성공() throws Exception {
        // given
        Long gymId = 1L;
        doNothing().when(likeService).postGymLike(anyLong(), eq(gymId));

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/like", gymId)
                .with(user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(likeService, times(1)).postGymLike(1L, gymId);
    }

    @Test
    @DisplayName("게시글 좋아요 - 성공")
    void postLike_성공() throws Exception {
        // given
        Long postId = 1L;
        doNothing().when(likeService).postLike(anyLong(), eq(postId));

        // when & then
        mockMvc.perform(post("/posts/{postId}/like", postId)
                .with(user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(likeService, times(1)).postLike(1L, postId);
    }

    @Test
    @DisplayName("헬스장 좋아요 - 인증되지 않은 사용자")
    void postGymLike_인증되지_않은_사용자() throws Exception {
        // given
        Long gymId = 1L;

        // when & then
        mockMvc.perform(post("/gyms/{gymId}/like", gymId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(likeService, never()).postGymLike(anyLong(), anyLong());
    }

    @Test
    @DisplayName("게시글 좋아요 - 인증되지 않은 사용자")
    void postLike_인증되지_않은_사용자() throws Exception {
        // given
        Long postId = 1L;

        // when & then
        mockMvc.perform(post("/posts/{postId}/like", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(likeService, never()).postLike(anyLong(), anyLong());
    }

    @Test
    @DisplayName("헬스장 좋아요 - 잘못된 gymId 타입")
    void postGymLike_잘못된_gymId() throws Exception {
        // when & then
        mockMvc.perform(post("/gyms/{gymId}/like", "invalid-id")
                .with(user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(likeService, never()).postGymLike(anyLong(), anyLong());
    }

    @Test
    @DisplayName("게시글 좋아요 - 잘못된 postId 타입")
    void postLike_잘못된_postId() throws Exception {
        // when & then
        mockMvc.perform(post("/posts/{postId}/like", "invalid-id")
                .with(user(mockUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(likeService, never()).postLike(anyLong(), anyLong());
    }
}
