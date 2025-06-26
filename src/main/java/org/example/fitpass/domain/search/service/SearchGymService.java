package org.example.fitpass.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
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
    private final org.example.fitpass.domain.gym.repository.GymRepository gymRepository;

    @Transactional(readOnly = true)
    @Cacheable(
        value = "gymSearch",
        key = "#keyword + '_' + #city + '_' + #district + '_' + (#pageable != null ? #pageable.pageNumber : 0) + '_' + (#pageable != null ? #pageable.pageSize : 20)"
    )
    public Page<GymResDto> searchGym (String keyword, String city, String district, Pageable pageable){

        if ("null".equalsIgnoreCase(keyword)) {
            keyword = null;
        }

        Page<Gym> gymPage = gymRepository.searchGym(keyword, city, district, pageable);

        return gymPage.map(GymResDto::from);
    }

    @Transactional
    public void saveSearchKeywordGym(String keyword) {
        if (keyword == null || keyword.isBlank() || "null".equalsIgnoreCase(keyword)) return;


        searchGymRepository.findByKeyword(keyword)
            .ifPresentOrElse(
                SearchKeywordGym::increaseCount,
                () -> searchGymRepository.save(new SearchKeywordGym(keyword))
            );
    }

}
