package org.example.fitpass.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.search.entity.SearchKeywordGym;
import org.example.fitpass.domain.search.repository.SearchGymRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class SearchGymService {

    private final SearchGymRepository searchGymRepository;
    private final GymRepository gymRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "gymSearch",
            keyGenerator =  "customKeyGenerator"
    )
    public Page<GymResDto> searchGym (String keyword, Pageable pageable){

        Page<Gym> gymPage = gymRepository.findByNameContaining(keyword,pageable);

        return gymPage.map(GymResDto::from);
    }

    @Transactional
    public void saveSearchKeywordGym(String keyword) {
        searchGymRepository.findByKeyword(keyword)
                .ifPresentOrElse(
                        SearchKeywordGym::increaseCount,
                        () -> searchGymRepository.save(new SearchKeywordGym(keyword))
                );
    }

}
