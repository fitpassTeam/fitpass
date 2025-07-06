package org.example.fitpass.domain.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
import org.example.fitpass.domain.post.dto.request.PostUpdateRequestDto;
import org.example.fitpass.domain.post.dto.response.PostImageResponseDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.service.PostService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> customStringRedisTemplate;

    @MockBean
    @Qualifier("notifyRedisTemplate")
    private RedisTemplate<String, List<Notify>> notifyRedisTemplate;


    @BeforeEach
    void setup() {
        // 원래 생성자 사용
        User user = new User(
                "test@test.com", "img.jpg", "pw", "테스트", "01012345678",
                25, "서울시", Gender.MAN, UserRole.USER
        );

        // ID를 강제로 1L로 반환하는 CustomUserDetails 익명 클래스 사용
        CustomUserDetails userDetails = new CustomUserDetails(user) {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public User getUser() {
                return super.getUser(); // User 내부에 ID 없이도 테스트가 가능할 경우
            }
        };

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("게시물 생성")
    @WithMockUser(username = "test@test.com")  // username 지정 가능
    void createPost() throws Exception {
        PostCreateRequestDto request = new PostCreateRequestDto(
                PostStatus.ACTIVE,
                PostType.GENERAL,
                "Test Title",
                "Test Content",
                List.of("image1.jpg")
        );

        PostResponseDto response = PostResponseDto.of(
                1L,
                PostStatus.ACTIVE,
                PostType.GENERAL,
                "Test Title",
                "Test Content",
                1L,
                10L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
            null,
            null
        );

        given(postService.createPost(
                request.status(),
                request.postType(),
                request.postImage(),
                request.title(),
                request.content(),
                1L,
                10L
        )).willReturn(response);

        mockMvc.perform(post("/gyms/10/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.postId").value(1L))
                .andExpect(jsonPath("$.data.title").value("Test Title"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.postType").value("GENERAL"));
    }

    @Test
    @DisplayName("General 게시물 전체 조회")
    @WithMockUser
    void findAllGeneralPosts() throws Exception {
        PostResponseDto post = PostResponseDto.of(
                1L,
                PostStatus.ACTIVE,
                PostType.GENERAL,
                "title",
                "content",
                1L,
                10L,
                LocalDateTime.now(),
                LocalDateTime.now(),
            null,
            null,
            null,
            null
        );
        Page<PostResponseDto> page = new PageImpl<>(List.of(post));

        given(postService.findAllPostByGeneral(
            any(Pageable.class),
            any(),  // nullable Long 타입으로 any() 쓰기
            eq(10L),
            eq(PostType.GENERAL)
        )).willReturn(page);

        mockMvc.perform(get("/gyms/10/general-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("title"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.content[0].postType").value("GENERAL"));
    }

    @Test
    @DisplayName("Notice 게시물 전체 조회")
    @WithMockUser
    void findAllNoticePosts() throws Exception {
        // Mock 데이터 생성
        PostResponseDto post = PostResponseDto.of(
                1L,
                PostStatus.ACTIVE,
                PostType.NOTICE,
                "Notice title",
                "Notice content",
                1L,
                10L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                null,
                null
        );

        // Service 메서드 mocking
        given(postService.findAllPostByNotice(
                any(Long.class),
                any(Long.class),
                any(PostType.class)
        )).willReturn(List.of(post));

        // 테스트 실행 및 검증 - HTTP 상태 200 OK만 확인
        mockMvc.perform(get("/gyms/10/notice-posts"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시물 단건 조회")
    @WithMockUser
    void findPostById() throws Exception {
        PostImageResponseDto response = new PostImageResponseDto(
                1L,
                List.of("image1.jpg", "image2.jpg"),
                PostStatus.ACTIVE,
                PostType.GENERAL,
                "Post Title",
                "Post Content",
                1L,
                10L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(postService.findPostById(
                any(User.class),
                eq(10L),
                eq(1L)
        )).willReturn(response);

        mockMvc.perform(get("/gyms/10/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(1L))
                .andExpect(jsonPath("$.data.postImage[0]").value("image1.jpg"))
                .andExpect(jsonPath("$.data.title").value("Post Title"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.postType").value("GENERAL"));
    }

    @Test
    @DisplayName("게시물 수정")
    @WithMockUser
    void updatePost() throws Exception {
        PostUpdateRequestDto request = new PostUpdateRequestDto(
                PostStatus.ACTIVE,
                PostType.NOTICE,
                "new title",
                "new content",
                List.of("new-image.jpg")
        );

        PostResponseDto response = PostResponseDto.of(
            1L,                    // postId
            PostStatus.ACTIVE,     // status
            PostType.NOTICE,       // postType
            "new title",           // title
            "new content",         // content
            1L,                    // userId
            10L,                   // gymId
            LocalDateTime.now(),   // createdAt
            LocalDateTime.now(),   // updatedAt
            List.of(),             // postImageUrl ← 빈 리스트
            0L,                    // likeCount ← 0
            0L,                    // commentCount ← 0
            false                  // isLiked ← false
        );

        given(postService.updatePost(
                5L,
                request.status(),
                request.postType(),
                request.title(),
                request.content(),
                1L,
                10L,
            List.of("new-image.jpg")
        )).willReturn(response);

        mockMvc.perform(patch("/gyms/10/posts/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("new title"))
                .andExpect(jsonPath("$.data.postType").value("NOTICE"))
                .andDo(print());
    }
}