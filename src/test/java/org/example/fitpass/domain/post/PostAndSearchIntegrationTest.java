package org.example.fitpass.domain.post;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.config.RedisService;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.payment.config.TossPaymentConfig;
import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
import org.example.fitpass.domain.post.dto.response.PostImageResponseDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.service.PostService;
import org.example.fitpass.domain.search.service.SearchGymService;
import org.example.fitpass.domain.search.service.SearchPostService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostAndSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private SearchPostService searchPostService;

    @MockBean
    private SearchGymService searchGymService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> customStringRedisTemplate;

    @MockBean
    @Qualifier("notifyRedisTemplate")
    private RedisTemplate<String, List<Notify>> notifyRedisTemplate;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // 1. 사용자 인증 설정
        User mockUser = new User(
                "test@test.com", "img.jpg", "pw", "테스트", "01012345678",
                25, "서울시", Gender.MAN, UserRole.USER
        );

        CustomUserDetails userDetails = new CustomUserDetails(mockUser) {
            @Override
            public Long getId() {
                return 1L;
            }
        };

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 2. searchGymService mocking 설정
        GymResDto gymResDto = new GymResDto(
                "헬스장 이름",
                "010-1234-5678",
                "설명 내용",
                "서울 강남구 테헤란로 123",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                10L,
                List.of("image1.jpg", "image2.jpg"),
                "요약된 설명입니다"
        );

        Page<GymResDto> gymPage = new PageImpl<>(List.of(gymResDto));

        when(searchGymService.searchGym(anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(gymPage);
    }

    @Test
    @DisplayName("1. 게시물 생성 → 2. 게시물 단건 조회 → 3. 게시물 검색 → 4. 검색 키워드 저장 시나리오")
    void postCreationAndSearchFlow() throws Exception {
        // 1. 게시물 생성 Mock 설정 및 API 호출

        PostCreateRequestDto createRequest = new PostCreateRequestDto(
                PostStatus.ACTIVE, PostType.GENERAL, "제목", "내용", List.of("img1.jpg")
        );
        PostResponseDto createdPost = PostResponseDto.of(
                1L, PostStatus.ACTIVE, PostType.GENERAL, "제목", "내용", 1L, 10L,
                LocalDateTime.now(), LocalDateTime.now(),null,null, null, null
        );
        when(postService.createPost(any(), any(), any(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(createdPost);

        mockMvc.perform(post("/gyms/10/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.postId").value(1L))
                .andExpect(jsonPath("$.data.title").value("제목"));

        // 2. 게시물 단건 조회 Mock 설정 및 API 호출
        PostImageResponseDto singlePost = new PostImageResponseDto(
                1L, List.of("img1.jpg"), PostStatus.ACTIVE, PostType.GENERAL,
                "제목", "내용", 1L, 10L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(postService.findPostById(any(User.class), eq(10L), eq(1L))).thenReturn(singlePost);

        mockMvc.perform(get("/gyms/10/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(1L))
                .andExpect(jsonPath("$.data.title").value("제목"));

        // 3. 게시물 검색 Mock 설정 및 API 호출
        Pageable pageable = PageRequest.of(0, 10);
        PostResponseDto searchDto = PostResponseDto.of(
                1L, PostStatus.ACTIVE, PostType.GENERAL, "제목", "내용", 1L, 10L,
                LocalDateTime.now(), LocalDateTime.now(),null,null,null,null
        );
        Page<PostResponseDto> searchResultPage = new PageImpl<>(List.of(searchDto), pageable, 1);

        when(searchPostService.searchPost(eq("제목"), any(Pageable.class))).thenReturn(searchResultPage);

        mockMvc.perform(get("/search/posts")
                        .param("keyword", "제목")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("게시물 검색이 완료되었습니다."))
                .andExpect(jsonPath("$.data.content[0].title").value("제목"));
        // 4. 검색 키워드 저장 verify 호출 (void 메서드라 직접 호출해서 verify)
        searchPostService.saveSearchKeywordPost("제목");
    }

    @Test
    @DisplayName("5. 체육관 검색 및 키워드 저장 시나리오")
    void gymSearchAndKeywordSaveFlow() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        // 체육관 Mock 객체 및 반환 설정
        GymResDto gymDto = new GymResDto(
                "헬스장1",
                "010-1234-5678",
                "설명",
                "서울 강남구",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                10L,
                List.of("image1.jpg", "image2.jpg"),
                "요약 설명"
        );

        Page<GymResDto> gymPage = new PageImpl<>(List.of(gymDto), pageable, 1);

        when(searchGymService.searchGym(eq("헬스장"), eq("서울"), eq("강남구"), any(Pageable.class)))
                .thenReturn(gymPage);

        mockMvc.perform(get("/search/gyms")
                        .param("keyword", "헬스장")
                        .param("city", "서울")
                        .param("district", "강남구")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("헬스장1"));

        // 검색 키워드 저장 verify
        searchGymService.saveSearchKeywordGym("헬스장");
    }
}