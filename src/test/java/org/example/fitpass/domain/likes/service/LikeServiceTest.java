package org.example.fitpass.domain.likes.service;

import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.likes.entity.Like;
import org.example.fitpass.domain.likes.repository.LikeRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LikeServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    private User user;
    private Like gymLike;
    private Like postLike;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 테스트용 User 객체 생성
        user = new User("test@email.com", "testUser", "LOCAL");
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 테스트용 Like 객체들 생성
        gymLike = Like.of(user, LikeType.GYM, 1L);
        postLike = Like.of(user, LikeType.POST, 1L);
    }

    @Test
    @DisplayName("헬스장 좋아요 - 처음 누르는 경우 (좋아요 추가)")
    void postGymLike_새로운_좋아요() {
        // given
        Long userId = 1L;
        Long gymId = 1L;
        
        when(userRepository.findByIdOrElseThrow(userId)).thenReturn(user);
        when(likeRepository.findByUserAndTargetId(user, gymId)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(gymLike);

        // when
        likeService.postGymLike(userId, gymId);

        // then
        verify(userRepository, times(1)).findByIdOrElseThrow(userId);
        verify(likeRepository, times(1)).findByUserAndTargetId(user, gymId);
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    @DisplayName("헬스장 좋아요 - 이미 눌러져 있는 경우 (좋아요 취소)")
    void postGymLike_좋아요_취소() {
        // given
        Long userId = 1L;
        Long gymId = 1L;
        
        when(userRepository.findByIdOrElseThrow(userId)).thenReturn(user);
        when(likeRepository.findByUserAndTargetId(user, gymId)).thenReturn(Optional.of(gymLike));

        // when
        likeService.postGymLike(userId, gymId);

        // then
        verify(userRepository, times(1)).findByIdOrElseThrow(userId);
        verify(likeRepository, times(1)).findByUserAndTargetId(user, gymId);
        verify(likeRepository, times(1)).delete(gymLike);
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("게시글 좋아요 - 처음 누르는 경우 (좋아요 추가)")
    void postLike_새로운_좋아요() {
        // given
        Long userId = 1L;
        Long postId = 1L;
        
        when(userRepository.findByIdOrElseThrow(userId)).thenReturn(user);
        when(likeRepository.findByUserAndTargetId(user, postId)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(postLike);

        // when
        likeService.postLike(userId, postId);

        // then
        verify(userRepository, times(1)).findByIdOrElseThrow(userId);
        verify(likeRepository, times(1)).findByUserAndTargetId(user, postId);
        verify(likeRepository, times(1)).save(argThat(like -> 
            like.getUser().equals(user) && 
            like.getTargetId().equals(postId) && 
            like.getLikeType().equals(LikeType.POST)
        ));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    @DisplayName("게시글 좋아요 - 이미 눌러져 있는 경우 (좋아요 취소)")
    void postLike_좋아요_취소() {
        // given
        Long userId = 1L;
        Long postId = 1L;
        
        when(userRepository.findByIdOrElseThrow(userId)).thenReturn(user);
        when(likeRepository.findByUserAndTargetId(user, postId)).thenReturn(Optional.of(postLike));

        // when
        likeService.postLike(userId, postId);

        // then
        verify(userRepository, times(1)).findByIdOrElseThrow(userId);
        verify(likeRepository, times(1)).findByUserAndTargetId(user, postId);
        verify(likeRepository, times(1)).delete(postLike);
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 헬스장 좋아요 시도 - 예외 발생")
    void postGymLike_존재하지_않는_사용자() {
        // given
        Long invalidUserId = 999L;
        Long gymId = 1L;
        
        when(userRepository.findByIdOrElseThrow(invalidUserId))
            .thenThrow(new BaseException(ExceptionCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> likeService.postGymLike(invalidUserId, gymId))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(userRepository, times(1)).findByIdOrElseThrow(invalidUserId);
        verify(likeRepository, never()).findByUserAndTargetId(any(), any());
        verify(likeRepository, never()).save(any());
        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 게시글 좋아요 시도 - 예외 발생")
    void postLike_존재하지_않는_사용자() {
        // given
        Long invalidUserId = 999L;
        Long postId = 1L;
        
        when(userRepository.findByIdOrElseThrow(invalidUserId))
            .thenThrow(new BaseException(ExceptionCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> likeService.postLike(invalidUserId, postId))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(userRepository, times(1)).findByIdOrElseThrow(invalidUserId);
        verify(likeRepository, never()).findByUserAndTargetId(any(), any());
        verify(likeRepository, never()).save(any());
        verify(likeRepository, never()).delete(any());
    }
}
