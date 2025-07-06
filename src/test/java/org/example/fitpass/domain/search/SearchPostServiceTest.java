package org.example.fitpass.domain.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.likes.repository.LikeRepository;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.search.entity.SearchKeywordPost;
import org.example.fitpass.domain.search.repository.SearchPostRepository;
import org.example.fitpass.domain.search.service.SearchPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class SearchPostServiceTest {

    @Mock
    private SearchPostRepository searchPostRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private SearchPostService searchPostService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 게시글_검색_성공_테스트() {
        // given
        String keyword = "운동";
        Pageable pageable = PageRequest.of(0, 10);
        Post post = mock(Post.class);
        Page<Post> postPage = new PageImpl<>(List.of(post));

        when(postRepository.searchByTitleOrContent(keyword, pageable)).thenReturn(postPage);
        when(likeRepository.countByLikeTypeAndTargetId(LikeType.POST, post.getId())).thenReturn(5L); // 좋아요 수 mock

        PostResponseDto dto = mock(PostResponseDto.class);
        try (MockedStatic<PostResponseDto> mockedStatic = mockStatic(PostResponseDto.class)) {
            mockedStatic.when(() -> PostResponseDto.from(post, 5L,null,null)).thenReturn(dto);

            // when
            Page<PostResponseDto> result = searchPostService.searchPost(keyword, pageable);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(dto);
            verify(postRepository).searchByTitleOrContent(keyword, pageable);
            verify(likeRepository).countByLikeTypeAndTargetId(LikeType.POST, post.getId()); // 이 부분도 검증
        }
    }

    @Test
    void 게시글_검색어_기존_존재시_카운트증가() {
        // given
        String keyword = "트레이너";
        SearchKeywordPost existing = mock(SearchKeywordPost.class);
        when(searchPostRepository.findByKeyword(keyword)).thenReturn(Optional.of(existing));

        // when
        searchPostService.saveSearchKeywordPost(keyword);

        // then
        verify(existing).increaseCount();
        verify(searchPostRepository, never()).save(any());
    }

    @Test
    void 게시글_검색어_존재하지않을경우_새로_저장() {
        // given
        String keyword = "식단";
        when(searchPostRepository.findByKeyword(keyword)).thenReturn(Optional.empty());

        // when
        searchPostService.saveSearchKeywordPost(keyword);

        // then
        verify(searchPostRepository).save(any(SearchKeywordPost.class));
    }
}
