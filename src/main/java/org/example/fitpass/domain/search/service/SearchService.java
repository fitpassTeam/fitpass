package org.example.fitpass.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.search.entity.SearchKeyword;
import org.example.fitpass.domain.search.repository.SearchRepository;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
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
    private final TrainerRepository trainerRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "gymSearch",
            keyGenerator =  "customKeyGenerator"
    )
    public Page<GymResDto> searchGym (String keyword, Pageable pageable){

        Page<Gym> gymPage = gymRepository.findByNameContaining(keyword,pageable);

        return gymPage.map(GymResDto::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "postSearch",
            key = "#keyword + '_' + (#pageable != null ? #pageable.pageNumber : 0) + '_' + (#pageable != null ? #pageable.pageSize : 20)"
    )
    public Page<PostResponseDto> searchPost (String keyword, Pageable pageable){

        Page<Post> postPage = postRepository.findBycontentAndPostType(keyword, pageable);

        return postPage.map(PostResponseDto::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "postSearch",
            key = "#keyword + '_' + (#pageable != null ? #pageable.pageNumber : 0) + '_' + (#pageable != null ? #pageable.pageSize : 20)"
    )
    public Page<TrainerResponseDto> searchTrainer (String keyword, Pageable pageable){

        Page<Trainer> trainerPage = trainerRepository.findByNameContaining(keyword, pageable);

        return trainerPage.map(TrainerResponseDto::fromEntity);
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
