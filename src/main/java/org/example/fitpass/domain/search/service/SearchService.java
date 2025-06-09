package org.example.fitpass.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.search.entity.SearchKeyword;
import org.example.fitpass.domain.search.repository.SearchRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class SearchService {

    private final SearchRepository searchRepository;
    private final GymRepository gymRepository;
    private final PostRepository postRepository;

    // 검색어 저장 (중복 검색어는 카운트 증가)



    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "gymSearch",
            keyGenerator =  "customKeyGenerator"
    )
    public Page<GymResponseDto> searchGym (String keyword, Pageable pageable){

        Page<Gym> gymPage = gymRepository.findByGymNameContaining(keyword,pageable);

        return gymPage.map(GymResponseDto::from);
    }

    @Cacheable(
            value = "postSearch",
            key = "#keyword + '_' + (#pageable != null ? #pageable.pageNumber : 0) + '_' + (#pageable != null ? #pageable.pageSize : 20)"
    )
    public Page<PostResponseDto> searchPost (String keyword, Pageable pageable){

        Page<Post> postPage = postRepository.findByPostNameContaining(keyword,pageable);

        return postPage.map(PostResponseDto::from);
    }

    @Transactional
    public void saveSearchKeyword(String keyword) {
        searchRepository.findByKeyword(keyword)
                .ifPresentOrElse(
                        SearchKeyword::increaseCount,
                        () -> searchRepository.save(new SearchKeyword(keyword))
                );
    }

}
