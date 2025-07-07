package org.example.fitpass.trainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.jwt.JwtTokenProvider;
import org.example.fitpass.common.config.RedisService;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.trainer.service.TrainerService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Trainer 통합 테스트")
class TrainerIntegrationTest {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> customStringRedisTemplate;

    @MockBean
    @Qualifier("notifyRedisTemplate")
    private RedisTemplate<String, List<Notify>> notifyRedisTemplate;

    private User ownerUser1;
    private User ownerUser2;
    private User normalUser;
    private Gym gym1;
    private Gym gym2;

    @BeforeEach
    void setUp() {
        trainerRepository.deleteAll();
        gymRepository.deleteAll();
        userRepository.deleteAll();

        // 첫 번째 체육관 오너
        ownerUser1 = new User(
            "owner1@test.com", null, "password123", "첫번째사장",
            "010-1111-1111", 35, "서울시 강남구",
            Gender.MAN, UserRole.OWNER, "LOCAL"
        );
        ownerUser1 = userRepository.save(ownerUser1);

        // 두 번째 체육관 오너
        ownerUser2 = new User(
            "owner2@test.com", null, "password123", "두번째사장",
            "010-2222-2222", 40, "서울시 서초구",
            Gender.WOMAN, UserRole.OWNER, "LOCAL"
        );
        ownerUser2 = userRepository.save(ownerUser2);

        // 일반 사용자
        normalUser = new User(
            "user@test.com", null, "password123", "일반사용자",
            "010-9999-9999", 25, "서울시 종로구",
            Gender.MAN, UserRole.USER, "LOCAL"
        );
        normalUser = userRepository.save(normalUser);

        // 첫 번째 체육관
        gym1 = Gym.of(
            List.of("gym1_1.jpg", "gym1_2.jpg"), "강남 피트니스", "02-1111-1111",
            "강남 최고의 헬스장", "서울", "강남구", "테헤란로 100",
            LocalTime.of(6, 0), LocalTime.of(23, 59), "24시간 운영", ownerUser1
        );
        gym1 = gymRepository.save(gym1);

        // 두 번째 체육관
        gym2 = Gym.of(
            List.of("gym2_1.jpg"), "서초 헬스클럽", "02-2222-2222",
            "서초동 프리미엄 헬스장", "서울", "서초구", "서초대로 200",
            LocalTime.of(5, 30), LocalTime.of(23, 30), "새벽 운영", ownerUser2
        );
        gym2 = gymRepository.save(gym2);
    }

    @Nested
    @DisplayName("통합 테스트 - 기본 CRUD")
    class IntegrationBasicCrud {

        @Test
        @DisplayName("트레이너 생성-조회-수정-삭제 전체 플로우")
        void trainerFullLifecycle() {
            // 1. 트레이너 생성
            TrainerResponseDto created = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "통합테스트트레이너", 55000, "통합 테스트용", "4년 경력",
                List.of("integration.jpg"));

            assertThat(created.id()).isNotNull();
            assertThat(created.name()).isEqualTo("통합테스트트레이너");
            assertThat(created.trainerStatus()).isEqualTo(TrainerStatus.ACTIVE);

            Long trainerId = created.id();

            // 2. 상세 조회
            TrainerDetailResponseDto retrieved = trainerService.getTrainerById(
                gym1.getId(), trainerId);
            assertThat(retrieved.name()).isEqualTo("통합테스트트레이너");
            assertThat(retrieved.price()).isEqualTo(55000);

            // 3. 수정
            TrainerResponseDto updated = trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), trainerId,
                "수정된트레이너", 65000, "수정된 내용", "6년 경력",
                TrainerStatus.HOLIDAY, List.of("updated.jpg"));

            assertThat(updated.name()).isEqualTo("수정된트레이너");
            assertThat(updated.price()).isEqualTo(65000);
            assertThat(updated.trainerStatus()).isEqualTo(TrainerStatus.HOLIDAY);

            // 4. 수정 확인
            TrainerDetailResponseDto reRetrieved = trainerService.getTrainerById(
                gym1.getId(), trainerId);
            assertThat(reRetrieved.name()).isEqualTo("수정된트레이너");
            assertThat(reRetrieved.trainerStatus()).isEqualTo(TrainerStatus.HOLIDAY);

            // 5. 삭제
            trainerService.deleteTrainer(ownerUser1.getId(), gym1.getId(), trainerId);

            // 6. 삭제 확인
            assertThatThrownBy(() -> trainerService.getTrainerById(gym1.getId(), trainerId))
                .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("데이터베이스 트랜잭션 무결성 검증")
        void transactionIntegrity() {
            // 트레이너 생성
            TrainerResponseDto trainer1 = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "트랜잭션테스트1", 50000, "테스트1", "3년",
                List.of("tx1.jpg"));

            TrainerResponseDto trainer2 = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "트랜잭션테스트2", 60000, "테스트2", "5년",
                List.of("tx2.jpg"));

            // 데이터베이스에서 직접 조회하여 검증
            List<Trainer> savedTrainers = trainerRepository.findAll();
            assertThat(savedTrainers).hasSize(2);
            
            // 체육관과의 연관관계 검증
            assertThat(savedTrainers).allMatch(trainer -> 
                trainer.getGym().getId().equals(gym1.getId()));

            // 삭제 후 트랜잭션 검증
            trainerService.deleteTrainer(ownerUser1.getId(), gym1.getId(), trainer1.id());
            
            List<Trainer> afterDelete = trainerRepository.findAll();
            assertThat(afterDelete).hasSize(1);
            assertThat(afterDelete.get(0).getId()).isEqualTo(trainer2.id());
        }
    }

    @Nested
    @DisplayName("통합 테스트 - 보안 및 권한")
    class IntegrationSecurity {

        @Test
        @DisplayName("체육관별 트레이너 관리 독립성 검증")
        void gymTrainerIsolation() {
            // 각 체육관에 트레이너 생성
            TrainerResponseDto gym1Trainer = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "강남트레이너", 80000, "강남 전문", "10년",
                List.of("gangnam.jpg"));

            TrainerResponseDto gym2Trainer = trainerService.createTrainer(
                ownerUser2.getId(), gym2.getId(),
                "서초트레이너", 75000, "서초 전문", "8년",
                List.of("seocho.jpg"));

            // 체육관별 조회 독립성 확인
            Page<TrainerResponseDto> gym1Trainers = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(0, 10));
            Page<TrainerResponseDto> gym2Trainers = trainerService.getAllTrainer(
                gym2.getId(), PageRequest.of(0, 10));

            assertThat(gym1Trainers.getContent()).hasSize(1);
            assertThat(gym2Trainers.getContent()).hasSize(1);
            assertThat(gym1Trainers.getContent().get(0).name()).isEqualTo("강남트레이너");
            assertThat(gym2Trainers.getContent().get(0).name()).isEqualTo("서초트레이너");

            // 크로스 체육관 접근 시도 (실패해야 함)
            assertThatThrownBy(() -> trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), gym2Trainer.id(),
                "해킹시도", 99999, "불법", "100년", TrainerStatus.ACTIVE, List.of()))
                .isInstanceOf(BaseException.class);

            // 권한 없는 사용자 접근 시도
            assertThatThrownBy(() -> trainerService.updateTrainer(
                normalUser.getId(), gym1.getId(), gym1Trainer.id(),
                "권한없음", 1, "실패", "0년", TrainerStatus.HOLIDAY, List.of()))
                .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("멀티 체육관 오너 권한 검증")
        void multiGymOwnerPermissions() {
            // 추가 체육관 생성 (오너1이 소유)
            Gym gym3 = Gym.of(
                List.of("gym3.jpg"), "강남 분점", "02-3333-3333",
                "강남 분점", "서울", "강남구", "역삼로 300",
                LocalTime.of(7, 0), LocalTime.of(22, 0), "분점", ownerUser1
            );
            final Gym savedGym3 = gymRepository.save(gym3);

            // 본점과 분점에 트레이너 생성
            TrainerResponseDto mainTrainer = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "본점트레이너", 80000, "본점", "10년", List.of("main.jpg"));

            TrainerResponseDto branchTrainer = trainerService.createTrainer(
                ownerUser1.getId(), savedGym3.getId(),
                "분점트레이너", 60000, "분점", "5년", List.of("branch.jpg"));

            // 올바른 체육관에서의 수정은 성공
            TrainerResponseDto updatedMain = trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), mainTrainer.id(),
                "수정된본점", 85000, "수정", "11년",
                TrainerStatus.ACTIVE, List.of("updated_main.jpg"));

            assertThat(updatedMain.name()).isEqualTo("수정된본점");

            // 잘못된 체육관에서의 수정은 실패
            assertThatThrownBy(() -> trainerService.updateTrainer(
                ownerUser1.getId(), savedGym3.getId(), mainTrainer.id(),
                "해킹", 1, "불가능", "0년", TrainerStatus.ACTIVE, List.of()))
                .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("시나리오 테스트")
    class ScenarioTests {

        @Test
        @DisplayName("시나리오: 체육관 운영 - 트레이너 채용부터 퇴사까지")
        void scenario_체육관_운영_전체_과정() {
            // 1단계: 신규 체육관 오픈 (트레이너 0명)
            Page<TrainerResponseDto> initialTrainers = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(0, 10));
            assertThat(initialTrainers.getContent()).isEmpty();

            // 2단계: 첫 번째 트레이너 채용
            TrainerResponseDto firstTrainer = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "김대표트레이너", 80000, "헤드 트레이너", "15년 경력",
                List.of("head_trainer.jpg"));

            // 3단계: 추가 트레이너들 채용
            TrainerResponseDto juniorTrainer1 = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "이주니어1", 45000, "신입 트레이너", "1년 경력",
                List.of("junior1.jpg"));

            TrainerResponseDto juniorTrainer2 = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "박주니어2", 50000, "신입 트레이너", "2년 경력",
                List.of("junior2.jpg"));

            // 4단계: 운영 현황 확인
            Page<TrainerResponseDto> allTrainers = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(0, 10));
            assertThat(allTrainers.getContent()).hasSize(3);

            // 5단계: 경력 증가로 인한 승급 및 급여 인상
            TrainerResponseDto promotedJunior = trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), juniorTrainer1.id(),
                "이시니어", 60000, "시니어 트레이너", "3년 경력",
                TrainerStatus.ACTIVE, List.of("promoted.jpg"));

            assertThat(promotedJunior.name()).isEqualTo("이시니어");
            assertThat(promotedJunior.price()).isEqualTo(60000);

            // 6단계: 한 트레이너의 휴가
            trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), juniorTrainer2.id(),
                juniorTrainer2.name(), juniorTrainer2.price(), juniorTrainer2.content(),
                juniorTrainer2.experience(), TrainerStatus.HOLIDAY, 
                List.of("vacation.jpg"));

            // 7단계: 휴가 중 트레이너 확인
            TrainerDetailResponseDto vacationTrainer = trainerService.getTrainerById(
                gym1.getId(), juniorTrainer2.id());
            assertThat(vacationTrainer.trainerStatus()).isEqualTo(TrainerStatus.HOLIDAY);

            // 8단계: 성과 미달로 인한 해고
            trainerService.deleteTrainer(ownerUser1.getId(), gym1.getId(), juniorTrainer2.id());

            // 9단계: 최종 운영진 확인
            Page<TrainerResponseDto> finalTrainers = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(0, 10));
            assertThat(finalTrainers.getContent()).hasSize(2);
            assertThat(finalTrainers.getContent())
                .extracting(TrainerResponseDto::name)
                .containsExactlyInAnyOrder("김대표트레이너", "이시니어");
        }

        @Test
        @DisplayName("시나리오: 시장 변화에 따른 운영 전략 변경")
        void scenario_시장_변화_대응() {
            // 1단계: 초기 운영진 구성 (다양한 가격대)
            TrainerResponseDto budget = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "저가형트레이너", 30000, "저가형", "1년", List.of("budget.jpg"));

            TrainerResponseDto standard = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "일반트레이너", 50000, "일반형", "3년", List.of("standard.jpg"));

            TrainerResponseDto premium = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "프리미엄트레이너", 80000, "프리미엄", "8년", List.of("premium.jpg"));

            // 2단계: 시장 상황 변화 - 경기 침체로 인한 가격 조정
            TrainerResponseDto adjustedBudget = trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), budget.id(),
                budget.name(), 25000, "가격 인하", budget.experience(),
                TrainerStatus.ACTIVE, List.of("adjusted.jpg"));

            TrainerResponseDto adjustedStandard = trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), standard.id(),
                standard.name(), 45000, "가격 인하", standard.experience(),
                TrainerStatus.ACTIVE, List.of("adjusted.jpg"));

            assertThat(adjustedBudget.price()).isEqualTo(25000);
            assertThat(adjustedStandard.price()).isEqualTo(45000);

            // 3단계: 고가 트레이너는 휴가 처리 (수요 감소)
            trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), premium.id(),
                premium.name(), premium.price(), premium.content(), premium.experience(),
                TrainerStatus.HOLIDAY, List.of("holiday.jpg"));

            // 4단계: 시장 회복 후 재조정
            TrainerResponseDto recoveredStandard = trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), standard.id(),
                "강화된일반트레이너", 55000, "시장 회복", "5년 경력",
                TrainerStatus.ACTIVE, List.of("recovered.jpg"));

            // 프리미엄 트레이너 복귀
            TrainerResponseDto reactivatedPremium = trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), premium.id(),
                premium.name(), 85000, "복귀", premium.experience(),
                TrainerStatus.ACTIVE, List.of("reactivated.jpg"));

            // 5단계: 최종 포트폴리오 확인
            Page<TrainerResponseDto> finalPortfolio = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(0, 10));

            assertThat(finalPortfolio.getContent()).hasSize(3);
            
            List<Integer> finalPrices = finalPortfolio.getContent().stream()
                .map(TrainerResponseDto::price)
                .sorted()
                .toList();
            assertThat(finalPrices).containsExactly(25000, 55000, 85000);

            // 모든 트레이너가 활성 상태인지 확인
            assertThat(finalPortfolio.getContent())
                .allMatch(trainer -> trainer.trainerStatus() == TrainerStatus.ACTIVE);
        }

        @Test
        @DisplayName("시나리오: 대용량 트레이너 데이터 처리")
        void scenario_대용량_데이터_처리() {
            // 1단계: 30명의 트레이너 생성
            for (int i = 1; i <= 30; i++) {
                trainerService.createTrainer(
                    ownerUser1.getId(), gym1.getId(),
                    "대량트레이너" + i, 50000 + (i * 100), "대량 트레이너 " + i,
                    (i % 10) + "년", List.of("bulk" + i + ".jpg"));
            }

            // 2단계: 페이징 성능 테스트
            Page<TrainerResponseDto> page1 = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(0, 10));
            Page<TrainerResponseDto> page2 = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(1, 10));
            Page<TrainerResponseDto> lastPage = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(2, 10));

            assertThat(page1.getTotalElements()).isEqualTo(30);
            assertThat(page1.getTotalPages()).isEqualTo(3);
            assertThat(page1.getContent()).hasSize(10);
            assertThat(lastPage.getContent()).hasSize(10);

            // 3단계: 대량 수정 테스트
            List<TrainerResponseDto> firstPageTrainers = page1.getContent();
            for (int i = 0; i < 5; i++) {
                TrainerResponseDto trainer = firstPageTrainers.get(i);
                trainerService.updateTrainer(
                    ownerUser1.getId(), gym1.getId(), trainer.id(),
                    "수정된" + trainer.name(), trainer.price() + 10000,
                    "대량수정", trainer.experience(), TrainerStatus.ACTIVE,
                    List.of("bulk_updated" + trainer.id() + ".jpg"));
            }

            // 4단계: 수정 결과 확인
            Page<TrainerResponseDto> updatedPage = trainerService.getAllTrainer(
                gym1.getId(), PageRequest.of(0, 20));
            
            long modifiedCount = updatedPage.getContent().stream()
                .filter(t -> t.name().startsWith("수정된"))
                .count();
            
            assertThat(modifiedCount).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("예외 상황 시나리오 테스트")
    class ExceptionScenarios {

        @Test
        @DisplayName("시나리오: 데이터 무결성 검증")
        void scenario_데이터_무결성_검증() {
            // 1단계: 정상 데이터로 트레이너 생성
            TrainerResponseDto trainer = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "무결성테스트트레이너", 50000, "무결성 테스트", "3년",
                List.of("integrity.jpg"));

            // 2단계: 존재하지 않는 체육관에 대한 접근 시도
            assertThatThrownBy(() -> trainerService.getTrainerById(999L, trainer.id()))
                .isInstanceOf(BaseException.class);

            // 3단계: 존재하지 않는 트레이너 접근 시도
            assertThatThrownBy(() -> trainerService.getTrainerById(gym1.getId(), 999L))
                .isInstanceOf(BaseException.class);

            // 4단계: 삭제된 트레이너 접근 시도
            trainerService.deleteTrainer(ownerUser1.getId(), gym1.getId(), trainer.id());
            
            assertThatThrownBy(() -> trainerService.getTrainerById(gym1.getId(), trainer.id()))
                .isInstanceOf(BaseException.class);

            assertThatThrownBy(() -> trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), trainer.id(),
                "삭제된트레이너", 60000, "불가능", "4년", TrainerStatus.ACTIVE, List.of()))
                .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("시나리오: 권한 검증 복합 상황")
        void scenario_권한_검증_복합상황() {
            // 1단계: 각 체육관에 트레이너 생성
            TrainerResponseDto gym1Trainer = trainerService.createTrainer(
                ownerUser1.getId(), gym1.getId(),
                "권한테스트1", 50000, "체육관1", "3년", List.of("auth1.jpg"));

            TrainerResponseDto gym2Trainer = trainerService.createTrainer(
                ownerUser2.getId(), gym2.getId(),
                "권한테스트2", 60000, "체육관2", "5년", List.of("auth2.jpg"));

            // 2단계: 크로스 체육관 접근 시도들
            // 오너1이 오너2의 체육관 트레이너에 접근
            assertThatThrownBy(() -> trainerService.updateTrainer(
                ownerUser1.getId(), gym2.getId(), gym2Trainer.id(),
                "불법접근", 1, "해킹", "0년", TrainerStatus.ACTIVE, List.of()))
                .isInstanceOf(BaseException.class);

            // 오너2가 오너1의 체육관 트레이너에 접근
            assertThatThrownBy(() -> trainerService.deleteTrainer(
                ownerUser2.getId(), gym1.getId(), gym1Trainer.id()))
                .isInstanceOf(BaseException.class);

            // 일반 사용자가 트레이너 생성 시도
            assertThatThrownBy(() -> trainerService.createTrainer(
                normalUser.getId(), gym1.getId(),
                "불법생성", 50000, "권한없음", "0년", List.of()))
                .isInstanceOf(BaseException.class);

            // 3단계: 정상 권한 확인
            TrainerResponseDto validUpdate = trainerService.updateTrainer(
                ownerUser1.getId(), gym1.getId(), gym1Trainer.id(),
                "정상수정", 55000, "권한확인", "4년", TrainerStatus.ACTIVE, List.of("valid.jpg"));

            assertThat(validUpdate.name()).isEqualTo("정상수정");
            assertThat(validUpdate.price()).isEqualTo(55000);
        }
    }
}
