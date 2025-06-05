package org.example.fitpass.trainer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.trainer.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    @InjectMocks
    private TrainerService trainerService;

    @Mock
    private TrainerRepository trainerRepository;

    private Trainer trainer;

    @BeforeEach
    void setUp(){
        trainer = Trainer.of(
            List.of(),"이름", 10000, "트레이너 정보"
        );

    }

    @Test
    @DisplayName("트레이너 생성 Test")
    void createTrainerTest(){
        //given
        given(trainerRepository.save(any(Trainer.class))).willReturn(trainer);

        //when
        TrainerResponseDto result = trainerService.createTrainer(
            "이름", 10000, "트레이너 정보", List.of()
        );
        //then
        assertThat(result.getName()).isEqualTo("이름");
        assertThat(result.getPrice()).isEqualTo(10000);
        assertThat(result.getContent()).isEqualTo("트레이너 정보");
    }

}
