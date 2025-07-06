package org.example.fitpass.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.comment.dto.response.CommentResponseDto;
import org.example.fitpass.domain.comment.entity.Comment;
import org.example.fitpass.domain.comment.repository.CommentRepository;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("CommentService 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private User anotherUser;
    private Post testPost;
    private Comment testComment;
    private Comment parentComment;

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        testUser = createUser(1L, "test@test.com", "테스트사용자");
        anotherUser = createUser(2L, "another@test.com", "다른사용자");
        
        // 테스트용 Post 생성
        testPost = createPost(1L, "테스트 제목", "테스트 내용", testUser);
        
        // 테스트용 Comment 생성
        testComment = createComment(1L, "테스트 댓글", testPost, testUser, null);
        parentComment = createComment(2L, "부모 댓글", testPost, testUser, null);
    }

    @Nested
    @DisplayName("댓글 생성 테스트")
    class CreateCommentTest {

        @Test
        @DisplayName("일반 댓글 생성 성공")
        void createComment_Success_WithoutParent() {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            String content = "새로운 댓글입니다.";
            Long parentId = null;

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(userRepository.findByIdOrElseThrow(userId)).willReturn(testUser);

            // When
            commentService.createComment(postId, userId, content, parentId);

            // Then
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("대댓글 생성 성공")
        void createComment_Success_WithParent() {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            String content = "대댓글입니다.";
            Long parentId = 2L;

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(userRepository.findByIdOrElseThrow(userId)).willReturn(testUser);
            given(commentRepository.findByIdOrElseThrow(parentId)).willReturn(parentComment);

            // When
            commentService.createComment(postId, userId, content, parentId);

            // Then
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("댓글 생성 실패 - 존재하지 않는 게시물")
        void createComment_Fail_PostNotFound() {
            // Given
            Long postId = 999L;
            Long userId = 1L;
            String content = "댓글 내용";
            Long parentId = null;

            given(postRepository.findByIdOrElseThrow(postId))
                    .willThrow(new BaseException(ExceptionCode.POST_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> commentService.createComment(postId, userId, content, parentId))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(ExceptionCode.POST_NOT_FOUND.getMessage());

            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("댓글 생성 실패 - 존재하지 않는 사용자")
        void createComment_Fail_UserNotFound() {
            // Given
            Long postId = 1L;
            Long userId = 999L;
            String content = "댓글 내용";
            Long parentId = null;

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(userRepository.findByIdOrElseThrow(userId))
                    .willThrow(new BaseException(ExceptionCode.USER_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> commentService.createComment(postId, userId, content, parentId))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(ExceptionCode.USER_NOT_FOUND.getMessage());

            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("대댓글 생성 실패 - 존재하지 않는 부모 댓글")
        void createComment_Fail_ParentCommentNotFound() {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            String content = "대댓글 내용";
            Long parentId = 999L;

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(userRepository.findByIdOrElseThrow(userId)).willReturn(testUser);
            given(commentRepository.findByIdOrElseThrow(parentId))
                    .willThrow(new BaseException(ExceptionCode.COMMENT_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> commentService.createComment(postId, userId, content, parentId))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(ExceptionCode.COMMENT_NOT_FOUND.getMessage());

            verify(commentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회 테스트")
    class GetCommentsTest {

        @Test
        @DisplayName("댓글 목록 조회 성공")
        void getComments_Success() {
            // Given
            Long postId = 1L;
            Comment comment1 = createComment(1L, "첫 번째 댓글", testPost, testUser, null);
            Comment comment2 = createComment(2L, "두 번째 댓글", testPost, anotherUser, null);
            List<Comment> comments = List.of(comment1, comment2);

            given(commentRepository.findByPostIdAndParentIsNull(postId)).willReturn(comments);

            // When
            List<CommentResponseDto> result = commentService.getComments(postId);

            // Then
            assertThat(result).hasSize(2);
            verify(commentRepository).findByPostIdAndParentIsNull(postId);
        }

        @Test
        @DisplayName("댓글이 없는 경우 빈 목록 반환")
        void getComments_EmptyList() {
            // Given
            Long postId = 1L;
            given(commentRepository.findByPostIdAndParentIsNull(postId)).willReturn(List.of());

            // When
            List<CommentResponseDto> result = commentService.getComments(postId);

            // Then
            assertThat(result).isEmpty();
            verify(commentRepository).findByPostIdAndParentIsNull(postId);
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class UpdateCommentTest {

        @Test
        @DisplayName("댓글 수정 성공 - 댓글 작성자")
        void updateComment_Success_CommentAuthor() {
            // Given
            Long commentId = 1L;
            Long userId = 1L;
            Long postId = 1L;
            String newContent = "수정된 댓글 내용";

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(commentRepository.findByIdOrElseThrow(commentId)).willReturn(testComment);
            given(userRepository.findByIdOrElseThrow(userId)).willReturn(testUser);

            // When
            commentService.updateComment(commentId, newContent, userId, postId);

            // Then
            verify(postRepository).findByIdOrElseThrow(postId);
            verify(commentRepository).findByIdOrElseThrow(commentId);
            verify(userRepository).findByIdOrElseThrow(userId);
        }

        @Test
        @DisplayName("댓글 수정 실패 - 권한 없음 (다른 사용자)")
        void updateComment_Fail_NoAuthority() {
            // Given
            Long commentId = 1L;
            Long userId = 2L; // 다른 사용자 ID
            Long postId = 1L;
            String newContent = "수정된 댓글 내용";

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(commentRepository.findByIdOrElseThrow(commentId)).willReturn(testComment);
            given(userRepository.findByIdOrElseThrow(userId)).willReturn(anotherUser);

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(commentId, newContent, userId, postId))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(ExceptionCode.NOT_GYM_OWNER.getMessage());
        }

        @Test
        @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
        void updateComment_Fail_CommentNotFound() {
            // Given
            Long commentId = 999L;
            Long userId = 1L;
            Long postId = 1L;
            String newContent = "수정된 내용";

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(commentRepository.findByIdOrElseThrow(commentId))
                    .willThrow(new BaseException(ExceptionCode.COMMENT_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(commentId, newContent, userId, postId))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(ExceptionCode.COMMENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteCommentTest {

        @Test
        @DisplayName("댓글 삭제 성공 - 댓글 작성자")
        void deleteComment_Success_CommentAuthor() {
            // Given
            Long commentId = 1L;
            Long userId = 1L;
            Long postId = 1L;

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(commentRepository.findByIdOrElseThrow(commentId)).willReturn(testComment);
            given(userRepository.findByIdOrElseThrow(userId)).willReturn(testUser);

            // When
            commentService.deleteComment(commentId, userId, postId);

            // Then
            verify(commentRepository).delete(testComment);
        }

        @Test
        @DisplayName("댓글 삭제 성공 - 게시글 작성자")
        void deleteComment_Success_PostAuthor() {
            // Given
            Long commentId = 1L;
            Long userId = 1L; // 게시글 작성자
            Long postId = 1L;
            Comment otherUserComment = createComment(2L, "다른 사용자 댓글", testPost, anotherUser, null);

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(commentRepository.findByIdOrElseThrow(commentId)).willReturn(otherUserComment);
            given(userRepository.findByIdOrElseThrow(userId)).willReturn(testUser);

            // When
            commentService.deleteComment(commentId, userId, postId);

            // Then
            verify(commentRepository).delete(otherUserComment);
        }

        @Test
        @DisplayName("댓글 삭제 실패 - 권한 없음")
        void deleteComment_Fail_NoAuthority() {
            // Given
            Long commentId = 1L;
            Long userId = 999L; // 권한 없는 사용자
            Long postId = 1L;
            User unauthorizedUser = createUser(999L, "unauthorized@test.com", "권한없는사용자");

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(commentRepository.findByIdOrElseThrow(commentId)).willReturn(testComment);
            given(userRepository.findByIdOrElseThrow(userId)).willReturn(unauthorizedUser);

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(commentId, userId, postId))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(ExceptionCode.NOT_HAS_AUTHORITY.getMessage());

            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
        void deleteComment_Fail_CommentNotFound() {
            // Given
            Long commentId = 999L;
            Long userId = 1L;
            Long postId = 1L;

            given(postRepository.findByIdOrElseThrow(postId)).willReturn(testPost);
            given(commentRepository.findByIdOrElseThrow(commentId))
                    .willThrow(new BaseException(ExceptionCode.COMMENT_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(commentId, userId, postId))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(ExceptionCode.COMMENT_NOT_FOUND.getMessage());

            verify(commentRepository, never()).delete(any());
        }
    }

    // 테스트 데이터 생성 헬퍼 메소드들
    private User createUser(Long id, String email, String name) {
        User user = new User(email, "profile.jpg", "password", 
                          name, "01012345678", 25, "서울시", Gender.MAN, UserRole.USER);
        // 리플렉션을 사용해서 ID 설정
        setField(user, "id", id);
        return user;
    }

    private Post createPost(Long id, String title, String content, User user) {
        Post post = new Post(PostStatus.ACTIVE, PostType.GENERAL, title, content, user, null);
        setField(post, "id", id);
        return post;
    }

    private Comment createComment(Long id, String content, Post post, User user, Comment parent) {
        Comment comment = Comment.of(post, user, content, parent);
        setField(comment, "id", id);
        return comment;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("필드 설정 실패: " + fieldName, e);
        }
    }
}