//package org.example.fitpass.domain.comment;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.example.fitpass.common.security.CustomUserDetails;
//import org.example.fitpass.domain.comment.dto.request.CommentRequestDto;
//import org.example.fitpass.domain.comment.dto.request.CommentUpdateRequestDto;
//import org.example.fitpass.domain.comment.entity.Comment;
//import org.example.fitpass.domain.comment.repository.CommentRepository;
//import org.example.fitpass.domain.post.entity.Post;
//import org.example.fitpass.domain.post.enums.PostStatus;
//import org.example.fitpass.domain.post.enums.PostType;
//import org.example.fitpass.domain.post.repository.PostRepository;
//import org.example.fitpass.domain.user.entity.User;
//import org.example.fitpass.domain.user.enums.Gender;
//import org.example.fitpass.domain.user.enums.UserRole;
//import org.example.fitpass.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Transactional
//@DisplayName("Comment 통합 테스트")
//class CommentIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private CommentRepository commentRepository;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User testUser;
//    private User anotherUser;
//    private Post testPost;
//    private Comment testComment;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트 데이터 초기화
//        commentRepository.deleteAll();
//        postRepository.deleteAll();
//        userRepository.deleteAll();
//
//        // 테스트 사용자 생성
//        testUser = createAndSaveUser("test@test.com", "테스트사용자");
//        anotherUser = createAndSaveUser("another@test.com", "다른사용자");
//
//        // 테스트 게시물 생성
//        testPost = createAndSavePost("테스트 제목", "테스트 내용", testUser);
//
//        // 테스트 댓글 생성
//        testComment = createAndSaveComment("테스트 댓글", testPost, testUser, null);
//
//        // 인증 설정
//        setupAuthentication(testUser);
//    }
//
//    @Test
//    @DisplayName("일반 댓글 생성 성공")
//    void createComment_Success() throws Exception {
//        // Given
//        CommentRequestDto request = new CommentRequestDto(null, "새로운 댓글입니다.");
//
//        // When & Then
//        mockMvc.perform(post("/posts/{postId}/comments", testPost.getId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//            .andDo(print())
//            .andExpect(status().isCreated())        // 여기서 201 Created 상태코드를 기대하고 있음
//            .andExpect(jsonPath("$.statusCode").value(201));  // 그런데 이 부분이 문제될 가능성 큼
//    }
//
//    @Test
//    @DisplayName("대댓글 생성 성공")
//    void createReplyComment_Success() throws Exception {
//        // Given
//        CommentRequestDto request = new CommentRequestDto(testComment.getId(), "대댓글입니다.");
//
//        // When & Then
//        mockMvc.perform(post("/posts/{postId}/comments", testPost.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isCreated());
//
//        // DB 검증
//        assertThat(commentRepository.countByPostId(testPost.getId())).isEqualTo(2L);
//
//        // 대댓글이 올바르게 생성되었는지 확인
//        var allComments = commentRepository.findAll();
//        var replyComment = allComments.stream()
//                .filter(c -> c.getParent() != null)
//                .findFirst()
//                .orElseThrow();
//
//        assertThat(replyComment.getParent().getId()).isEqualTo(testComment.getId());
//        assertThat(replyComment.getContent()).isEqualTo("대댓글입니다.");
//    }
//
//    @Test
//    @DisplayName("댓글 목록 조회 성공")
//    void getComments_Success() throws Exception {
//        // Given
//        createAndSaveComment("대댓글", testPost, anotherUser, testComment);
//
//        // When & Then
//        mockMvc.perform(get("/posts/{postId}/comments", testPost.getId()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").isArray())
//                .andExpect(jsonPath("$.data[0].id").value(testComment.getId()))
//                .andExpect(jsonPath("$.data[0].content").value("테스트 댓글"));
//    }
//
//    @Test
//    @DisplayName("댓글 수정 성공")
//    void updateComment_Success() throws Exception {
//        // Given
//        CommentUpdateRequestDto request = new CommentUpdateRequestDto("수정된 댓글 내용");
//
//        // When & Then
//        mockMvc.perform(patch("/posts/{postId}/comments/{commentId}",
//                        testPost.getId(), testComment.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        // DB 검증
//        Comment updatedComment = commentRepository.findById(testComment.getId()).orElseThrow();
//        assertThat(updatedComment.getContent()).isEqualTo("수정된 댓글 내용");
//    }
//
//    @Test
//    @DisplayName("댓글 수정 실패 - 다른 사용자의 댓글")
//    void updateComment_Fail_DifferentUser() throws Exception {
//        // Given
//        setupAuthentication(anotherUser); // 다른 사용자로 인증 변경
//        CommentUpdateRequestDto request = new CommentUpdateRequestDto("수정된 내용");
//
//        // When & Then
//        mockMvc.perform(patch("/posts/{postId}/comments/{commentId}",
//                        testPost.getId(), testComment.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // DB 검증 - 내용이 변경되지 않았는지 확인
//        Comment unchangedComment = commentRepository.findById(testComment.getId()).orElseThrow();
//        assertThat(unchangedComment.getContent()).isEqualTo("테스트 댓글");
//    }
//
//    @Test
//    @DisplayName("댓글 삭제 성공 - 댓글 작성자")
//    void deleteComment_Success_CommentAuthor() throws Exception {
//        // When & Then
//        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}",
//                        testPost.getId(), testComment.getId()))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        // DB 검증
//        assertThat(commentRepository.findById(testComment.getId())).isEmpty();
//    }
//
//    @Test
//    @DisplayName("댓글 삭제 성공 - 게시글 작성자")
//    void deleteComment_Success_PostAuthor() throws Exception {
//        // Given
//        Comment otherUserComment = createAndSaveComment("다른 사용자 댓글", testPost, anotherUser, null);
//
//        // When & Then (testUser는 게시글 작성자)
//        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}",
//                        testPost.getId(), otherUserComment.getId()))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        // DB 검증
//        assertThat(commentRepository.findById(otherUserComment.getId())).isEmpty();
//    }
//
//    @Test
//    @DisplayName("댓글 삭제 실패 - 권한 없음")
//    void deleteComment_Fail_NoAuthority() throws Exception {
//        // Given
//        setupAuthentication(anotherUser); // 권한 없는 사용자로 변경
//
//        // When & Then
//        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}",
//                        testPost.getId(), testComment.getId()))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//
//        // DB 검증 - 댓글이 삭제되지 않았는지 확인
//        assertThat(commentRepository.findById(testComment.getId())).isPresent();
//    }
//
//    @Test
//    @DisplayName("댓글 생성 실패 - 존재하지 않는 게시물")
//    void createComment_Fail_PostNotFound() throws Exception {
//        // Given
//        CommentRequestDto request = new CommentRequestDto(null, "댓글 내용");
//
//        // When & Then
//        mockMvc.perform(post("/posts/999/comments")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404));
//
//        // DB 검증 - 댓글이 생성되지 않았는지 확인
//        assertThat(commentRepository.countByPostId(testPost.getId())).isEqualTo(1L);
//    }
//
//    @Test
//    @DisplayName("댓글이 없는 게시물 조회")
//    void getComments_EmptyPost() throws Exception {
//        // Given
//        Post emptyPost = createAndSavePost("빈 게시물", "댓글이 없는 게시물", testUser);
//
//        // When & Then
//        mockMvc.perform(get("/posts/{postId}/comments", emptyPost.getId()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").isArray())
//                .andExpect(jsonPath("$.data").isEmpty());
//    }
//
//    // 헬퍼 메소드들
//    private User createAndSaveUser(String email, String name) {
//        User user = new User(email, "profile.jpg", "password",
//                           name, "01012345678", 25, "서울시", Gender.MAN, UserRole.USER);
//        return userRepository.save(user);
//    }
//
//    private Post createAndSavePost(String title, String content, User user) {
//        Post post = new Post(PostStatus.ACTIVE, PostType.GENERAL, title, content, user, null);
//        return postRepository.save(post);
//    }
//
//    private Comment createAndSaveComment(String content, Post post, User user, Comment parent) {
//        Comment comment = Comment.of(post, user, content, parent);
//        return commentRepository.save(comment);
//    }
//
//    private void setupAuthentication(User user) {
//        CustomUserDetails userDetails = new CustomUserDetails(user) {
//            @Override
//            public Long getId() {
//                return user.getId();
//            }
//        };
//
//        UsernamePasswordAuthenticationToken authentication =
//                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//}