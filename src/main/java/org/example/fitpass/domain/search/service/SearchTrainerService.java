package org.example.fitpass.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.search.entity.SearchKeywordTrainer;
import org.example.fitpass.domain.search.repository.SearchTrainerRepository;
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

public class SearchTrainerService {

    private final SearchTrainerRepository searchTrainerRepository;
    private final TrainerRepository trainerRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            value = "trainerSearch",
            key = "#keyword + '_' + (#pageable != null ? #pageable.pageNumber : 0) + '_' + (#pageable != null ? #pageable.pageSize : 20)"
    )
    public Page<TrainerResponseDto> searchTrainer (String keyword, Pageable pageable){

        Page<Trainer> trainerPage = trainerRepository.findByNameContaining(keyword, pageable);

        return trainerPage.map(TrainerResponseDto::fromEntity);
    }

    @Transactional
    public void saveSearchKeywordTrainer(String keyword) {
        searchTrainerRepository.findByKeyword(keyword)
                .ifPresentOrElse(
                        SearchKeywordTrainer::increaseCount,
                        () -> searchTrainerRepository.save(new SearchKeywordTrainer(keyword))
                );
    }

}
