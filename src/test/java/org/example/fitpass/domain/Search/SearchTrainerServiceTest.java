package org.example.fitpass.domain.Search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.example.fitpass.domain.search.entity.SearchKeywordTrainer;
import org.example.fitpass.domain.search.repository.SearchTrainerRepository;
import org.example.fitpass.domain.search.service.SearchTrainerService;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

class SearchTrainerServiceTest {

    @Mock
    private SearchTrainerRepository searchTrainerRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private SearchTrainerService searchTrainerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 트레이너_검색_정상작동() {
        // given
        String keyword = "홍길동";
        Pageable pageable = PageRequest.of(0, 10);

        Trainer trainer = new Trainer(); // 실제 필요한 필드가 있다면 채워 넣기
        Page<Trainer> trainerPage = new PageImpl<>(List.of(trainer));

        when(trainerRepository.findByNameContaining(keyword, pageable)).thenReturn(trainerPage);

        // when
        Page<TrainerResponseDto> result = searchTrainerService.searchTrainer(keyword, pageable);

        // then
        assertThat(result).hasSize(1);
        verify(trainerRepository).findByNameContaining(keyword, pageable);
    }

    @Test
    void 트레이너_키워드_존재할경우_카운트_증가() {
        // given
        String keyword = "엄복동";

        SearchKeywordTrainer existing = mock(SearchKeywordTrainer.class);
        when(searchTrainerRepository.findByKeyword(keyword)).thenReturn(Optional.of(existing));

        // when
        searchTrainerService.saveSearchKeywordTrainer(keyword);

        // then
        verify(existing).increaseCount();
        verify(searchTrainerRepository, never()).save(any());
    }

    @Test
    void 트레이너_키워드_없을경우_새로저장() {
        // given
        String keyword = "엄준식";

        when(searchTrainerRepository.findByKeyword(keyword)).thenReturn(Optional.empty());

        // when
        searchTrainerService.saveSearchKeywordTrainer(keyword);

        // then
        verify(searchTrainerRepository).save(any(SearchKeywordTrainer.class));
    }
}