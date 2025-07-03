package org.example.fitpass.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymRepository gymRepository;

    @InjectMocks
    private PostService postService;

    private User mockUser;
    private Gym mockGym;

    @BeforeEach
    void setUp() {
        // Mockito 초기화는 @ExtendWith(MockitoExtension.class)에서 자동 처리됨

        mockUser = new User(
                "test@email.com",
                "userImage.jpg",
                "encodedPassword",
                "테스트유저",
                "010-1234-5678",
                25,
                "서울 강남구",
                Gender.MAN,
                UserRole.USER
        );

        mockGym = new Gym(
                List.of(),
                "테스트 헬스장",
                "010-9999-8888",
                "헬스장 설명",
                "서울",
                "강남구",
                "테헤란로 123",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                mockUser
        );

        setId(mockUser, 1L);
        setId(mockGym, 1L);
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }

    @Test
    void createPost_성공적으로_게시글_생성() {
        // given
        Long userId = 1L;
        Long gymId = 1L;
        PostType postType = PostType.GENERAL;
        PostStatus postStatus = PostStatus.ACTIVE;
        List<String> images = List.of("img1.jpg", "img2.jpg");
        String title = "제목입니다";
        String content = "내용입니다";

        when(userRepository.findByIdOrElseThrow(userId)).thenReturn(mockUser);
        when(gymRepository.findByIdOrElseThrow(gymId)).thenReturn(mockGym);
        when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PostResponseDto result = postService.createPost(
                postStatus, postType, images, title, content, userId, gymId
        );

        // then
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.postType()).isEqualTo(postType);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.gymId()).isEqualTo(gymId);
    }


    @Test
    void createPost_공지사항인데_작성자가_OWNER가_아닌_경우_예외발생() {
        // given
        Long userId = 1L;
        Long gymId = 1L;

        PostType postType = PostType.NOTICE;
        PostStatus postStatus = PostStatus.ACTIVE;
        List<String> images = List.of("img.jpg");
        String title = "공지사항입니다";
        String content = "내용";

        when(userRepository.findByIdOrElseThrow(userId)).thenReturn(mockUser);
        when(gymRepository.findByIdOrElseThrow(gymId)).thenReturn(mockGym);

        // when & then
        BaseException exception = assertThrows(BaseException.class, () ->
                postService.createPost(postStatus, postType, images, title, content, userId, gymId)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ExceptionCode.NOTICE_ONLY_OWNER);
    }

    @Test
    void createPost_OWNER인_사용자는_공지사항_작성_가능() {
        // given
        mockUser.approveOwnerUpgrade(); // OWNER 권한 부여

        Long userId = 1L;
        Long gymId = 1L;
        PostType postType = PostType.NOTICE;
        PostStatus postStatus = PostStatus.ACTIVE;
        List<String> images = List.of("img1.jpg");
        String title = "공지 제목";
        String content = "공지 내용";

        when(userRepository.findByIdOrElseThrow(userId)).thenReturn(mockUser);
        when(gymRepository.findByIdOrElseThrow(gymId)).thenReturn(mockGym);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PostResponseDto result = postService.createPost(
                postStatus, postType, images, title, content, userId, gymId
        );

        // then
        assertThat(result.postType()).isEqualTo(PostType.NOTICE);
        assertThat(result.userId()).isEqualTo(userId);
    }

}