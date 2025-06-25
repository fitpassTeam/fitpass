package org.example.fitpass.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.search.entity.SearchKeywordPost;
import org.example.fitpass.domain.search.repository.SearchPostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class SearchPostService {

    private final SearchPostRepository searchPostRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            value = "postSearch",
            key = "#keyword + '_' + (#pageable != null ? #pageable.pageNumber : 0) + '_' + (#pageable != null ? #pageable.pageSize : 20)"
    )
    public Page<PostResponseDto> searchPost (String keyword, Pageable pageable){

        Page<Post> postPage = postRepository.searchByTitleOrContent(keyword, pageable);

        return postPage.map(PostResponseDto::from);
    }

    @Transactional
    public void saveSearchKeywordPost(String keyword) {
        searchPostRepository.findByKeyword(keyword)
                .ifPresentOrElse(
                        SearchKeywordPost::increaseCount,
                        () -> searchPostRepository.save(new SearchKeywordPost(keyword))
                );
    }

}
