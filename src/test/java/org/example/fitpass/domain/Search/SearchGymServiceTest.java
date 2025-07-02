package org.example.fitpass.domain.Search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.search.entity.SearchKeywordGym;
import org.example.fitpass.domain.search.repository.SearchGymRepository;
import org.example.fitpass.domain.search.service.SearchGymService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

class SearchGymServiceTest {

    @Mock
    private SearchGymRepository searchGymRepository;

    @Mock
    private GymRepository gymRepository;

    @InjectMocks
    private SearchGymService searchGymService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 체육관_조회시_키워드로_검색시_Dto_반환() {
        // given
        String keyword = "헬스장";
        String city = "서울";
        String district = "강남구";
        Pageable pageable = PageRequest.of(0, 10);

        // Gym 실제 객체 생성 (mock 아님)
        Gym gym = new Gym(
                List.of(),  // 이미지 리스트는 빈 리스트로
                "헬스장 이름",
                "010-1234-5678",
                "설명 내용",
                "서울",
                "강남구",
                "테헤란로 123",
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                null  // 필요시 User 객체 넣기
        );

        // Gym에 ID 필드가 있다면 Reflection으로 세팅
        setId(gym, 1L);

        Page<Gym> gymPage = new PageImpl<>(List.of(gym));

        when(gymRepository.searchGym(keyword, city, district, pageable)).thenReturn(gymPage);

        // when
        Page<GymResDto> result = searchGymService.searchGym(keyword, city, district, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        GymResDto dto = result.getContent().get(0);
        assertThat(dto.name()).isEqualTo("헬스장 이름");
        assertThat(dto.number()).isEqualTo("010-1234-5678");
        assertThat(dto.content()).isEqualTo("설명 내용");
        assertThat(dto.address()).contains("서울", "강남구");
        assertThat(dto.openTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(dto.closeTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(dto.gymId()).isEqualTo(1L);

        verify(gymRepository).searchGym(keyword, city, district, pageable);
    }

    // setId() 메서드: 리플렉션으로 id 세팅
    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("ID 설정 실패", e);
        }
    }

    @Test
    void 키워드가_Null_또는_빈값일_때_저장하지_않음() {
        searchGymService.saveSearchKeywordGym(null);
        searchGymService.saveSearchKeywordGym("");
        searchGymService.saveSearchKeywordGym("   ");
        searchGymService.saveSearchKeywordGym("null");

        verifyNoInteractions(searchGymRepository);
    }

    @Test
    void 키워드가_존재하면_카운트_증가() {
        // given
        String keyword = "헬스장";
        SearchKeywordGym existing = mock(SearchKeywordGym.class);

        when(searchGymRepository.findByKeyword(keyword))
                .thenReturn(Optional.of(existing));

        // when
        searchGymService.saveSearchKeywordGym(keyword);

        // then
        verify(existing).increaseCount(); // count 증가는 호출됨
        verify(searchGymRepository, never()).save(any()); // save는 호출되지 않아야 함
    }

    @Test
    void 키워드가_존재하지_않으면_새로_저장() {
        String keyword = "요가";

        when(searchGymRepository.findByKeyword(keyword)).thenReturn(Optional.empty());

        searchGymService.saveSearchKeywordGym(keyword);

        verify(searchGymRepository).save(any(SearchKeywordGym.class));
    }

    @Test
    void 인기_키워드_조회_정확한_키워드_반환() {
        SearchKeywordGym k1 = mock(SearchKeywordGym.class);
        SearchKeywordGym k2 = mock(SearchKeywordGym.class);

        when(k1.getKeyword()).thenReturn("헬스장");
        when(k2.getKeyword()).thenReturn("요가");

        when(searchGymRepository.findTop5ByOrderByCountDesc()).thenReturn(List.of(k1, k2));

        List<String> result = searchGymService.searchPopularGym();

        assertThat(result).containsExactly("헬스장", "요가");
    }
}