package org.example.fitpass.domain.search.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.search.entity.SearchKeywordGym;
import org.example.fitpass.domain.search.repository.SearchGymRepository;
import org.springframework.cache.annotation.CacheEvict;
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

    // Set: 중복 없음, 검색 속도 빠름
    private static final Set<String> GYM_KEYWORDS = Set.of(
        "헬스장", "피트니스", "체육관", "스포츠센터", "짐", "gym",
        "크로스핏", "요가", "필라테스", "수영장", "클라이밍"
    );

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
    @CacheEvict(value = "popularKeywords", allEntries = true)
    public void saveSearchKeywordGym(String keyword) {
        if (keyword == null || keyword.isBlank() || "null".equalsIgnoreCase(keyword)) return;

        if (!isGymRelatedKeyword(keyword)) return;

        searchGymRepository.findByKeyword(keyword)
            .ifPresentOrElse(
                SearchKeywordGym::increaseCount,
                () -> searchGymRepository.save(new SearchKeywordGym(keyword))
            );
    }

    @Transactional(readOnly = true)
    @Cacheable("popularKeywords")
    public List<String> searchPopularGym() {
        return searchGymRepository.findTop5ByOrderByCountDesc()
            .stream()
            .map(SearchKeywordGym::getKeyword)
            .toList();
    }

    private boolean isGymRelatedKeyword(String keyword) {
        return GYM_KEYWORDS.stream()
            .anyMatch(gymKeyword -> keyword.toLowerCase().contains(gymKeyword));
    }
}
