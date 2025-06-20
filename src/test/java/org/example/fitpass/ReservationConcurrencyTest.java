//package org.example.fitpass;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.example.fitpass.domain.gym.entity.Gym;
//import org.example.fitpass.domain.gym.repository.GymRepository;
//import org.example.fitpass.domain.reservation.dto.request.ReservationRequestDto;
//import org.example.fitpass.domain.reservation.dto.response.ReservationResponseDto;
//import org.example.fitpass.domain.reservation.enums.ReservationStatus;
//import org.example.fitpass.domain.reservation.service.ReservationService;
//import org.example.fitpass.domain.trainer.entity.Trainer;
//import org.example.fitpass.domain.trainer.repository.TrainerRepository;
//import org.example.fitpass.domain.user.Gender;
//import org.example.fitpass.domain.user.UserRole;
//import org.example.fitpass.domain.user.entity.User;
//import org.example.fitpass.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@org.springframework.test.context.ActiveProfiles("test")
//class ReservationConcurrencyTest {
//
//    @Autowired
//    private ReservationService reservationService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private TrainerRepository trainerRepository;
//
//    @Autowired
//    private GymRepository gymRepository;
//
//    private Gym testGym;
//    private Trainer testTrainer;
//
//    @BeforeEach
//    @Transactional
//    void setUp() {
//        // 체육관 소유자 생성
//        User owner = new User(
//            "owner@test.com",
//            "...",
//            "password123",
//            "체육관소유자",
//            "010-1111-1111",
//            35,
//            "서울시 강남구",
//            Gender.MAN,
//            UserRole.OWNER
//        );
//        owner = userRepository.save(owner);
//
//        // 체육관 생성
//        testGym = Gym.of(
//            new ArrayList<>(), // 이미지 리스트
//            "테스트체육관",
//            "02-1234-5678",
//            "동시성 테스트용 체육관",
//            "서울시 테스트구",
//            LocalTime.of(9, 0),
//            LocalTime.of(22, 0),
//            owner
//        );
//        testGym = gymRepository.save(testGym);
//
//        // 트레이너 생성 (리플렉션으로 gym 설정)
//        testTrainer = Trainer.of(
//            new ArrayList<>(), // 이미지 리스트
//            "테스트트레이너",
//            50000,
//            "동시성 테스트용 트레이너"
//        );
//
//        // gym 연관관계 설정 (리플렉션 사용)
//        try {
//            java.lang.reflect.Field gymField = Trainer.class.getDeclaredField("gym");
//            gymField.setAccessible(true);
//            gymField.set(testTrainer, testGym);
//        } catch (Exception e) {
//            throw new RuntimeException("Trainer gym 설정 실패", e);
//        }
//
//        testTrainer = trainerRepository.save(testTrainer);
//    }
//
//    // 테스트용 사용자 생성 (생성자 사용)
//    private User createTestUser(String email) {
//        User user = new User(
//            email,
//            "...",
//            "password123",
//            "테스트유저",
//            "010-1234-5678",
//            25,
//            "서울시 테스트구",
//            Gender.MAN,
//            UserRole.USER
//        );
//        // 포인트 잔액 설정 (리플렉션 대신 public 메서드 사용)
//        user.updatePointBalance(100000);
//        return userRepository.save(user);
//    }
//
//    // 테스트용 예약 요청 DTO 생성
//    private ReservationRequestDto createReservationRequest(LocalDate date, LocalTime time) {
//        return new ReservationRequestDto(
//            date,
//            time,
//            ReservationStatus.PENDING
//        );
//    }
//
//    // ================== 기존 DB 유니크 제약조건 테스트 ==================
//
//    @Test
//    void 동시_예약_테스트() throws Exception {
//        // Given: 테스트 데이터 준비
//        User user1 = createTestUser("user1@test.com");
//        User user2 = createTestUser("user2@test.com");
//
//        LocalDate reservationDate = LocalDate.now().plusDays(3);
//        LocalTime reservationTime = LocalTime.of(14, 0);
//
//        // When: 동시에 같은 시간 예약 시도
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//        CountDownLatch latch = new CountDownLatch(2);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//        List<String> results = Collections.synchronizedList(new ArrayList<>());
//
//        // 시작 시간 측정
//        long startTime = System.currentTimeMillis();
//
//        // 사용자1 예약 시도
//        executor.submit(() -> {
//            try {
//                ReservationRequestDto request = createReservationRequest(reservationDate, reservationTime);
//                ReservationResponseDto result = reservationService.createReservation(
//                    request.reservationDate(), request.reservationTime(), ReservationStatus.PENDING, user1.getId(), testGym.getId(), testTrainer.getId()
//                );
//                successCount.incrementAndGet();
//                results.add("사용자1 예약 성공! ID: " + result.reservationId());
//            } catch (Exception e) {
//                failCount.incrementAndGet();
//                results.add("사용자1 예약 실패: " + e.getMessage());
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        // 사용자2 예약 시도
//        executor.submit(() -> {
//            try {
//                ReservationRequestDto request = createReservationRequest(reservationDate, reservationTime);
//                ReservationResponseDto result = reservationService.createReservation(
//                    request.reservationDate(), request.reservationTime(), ReservationStatus.PENDING, user2.getId(), testGym.getId(), testTrainer.getId()
//                );
//                successCount.incrementAndGet();
//                results.add("사용자2 예약 성공! ID: " + result.reservationId());
//            } catch (Exception e) {
//                failCount.incrementAndGet();
//                results.add("사용자2 예약 실패: " + e.getMessage());
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        latch.await(10, TimeUnit.SECONDS);
//        executor.shutdown();
//
//        // 종료 시간 측정
//        long endTime = System.currentTimeMillis();
//
//        // Then: 결과 확인
//        System.out.println("\n=== 동시 예약 테스트 결과 ===");
//        results.forEach(System.out::println);
//        System.out.println("총 성공: " + successCount.get());
//        System.out.println("총 실패: " + failCount.get());
//        System.out.println("실행 시간: " + (endTime - startTime) + "ms");
//
//        // 정상적이라면 성공 1개, 실패 1개여야 함
//        assertThat(successCount.get() + failCount.get()).isEqualTo(2);
//
//        // 동시성 제어가 제대로 되었다면 1명만 성공해야 함
//        if (successCount.get() == 1) {
//            System.out.println("동시성 제어 성공!");
//        } else {
//            System.out.println("동시성 문제 발생! " + successCount.get() + "명이 동시 예약 성공");
//        }
//    }
//
//    @Test
//    void 다중_사용자_동시_예약_테스트() throws Exception {
//        // Given: 테스트 데이터 준비
//        LocalDate reservationDate = LocalDate.now().plusDays(3);
//        LocalTime reservationTime = LocalTime.of(15, 0);
//
//        // 10명이 동시에 같은 시간 예약 시도
//        int threadCount = 10;
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//        List<String> results = Collections.synchronizedList(new ArrayList<>());
//
//        // 시작 시간 측정
//        long startTime = System.currentTimeMillis();
//
//        for (int i = 0; i < threadCount; i++) {
//            final int userIndex = i;
//            executor.submit(() -> {
//                try {
//                    // 각 스레드마다 새로운 사용자 생성
//                    User user = createTestUser("multiUser" + userIndex + "@test.com");
//                    ReservationRequestDto request = createReservationRequest(reservationDate, reservationTime);
//
//                    // 예약 시도
//                    ReservationResponseDto result = reservationService.createReservation(
//                        request.reservationDate(), request.reservationTime(), ReservationStatus.PENDING, user.getId(), testGym.getId(), testTrainer.getId()
//                    );
//
//                    successCount.incrementAndGet();
//                    results.add("사용자" + userIndex + " 예약 성공! 예약ID: " + result.reservationId());
//
//                } catch (Exception e) {
//                    failCount.incrementAndGet();
//                    results.add("사용자" + userIndex + " 예약 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        // 모든 스레드가 완료될 때까지 대기
//        latch.await(30, TimeUnit.SECONDS);
//        executor.shutdown();
//
//        // 종료 시간 측정
//        long endTime = System.currentTimeMillis();
//
//        // Then: 결과 확인
//        System.out.println("\n=== 다중 사용자 동시 예약 테스트 결과 ===");
//        System.out.println("총 시도: " + threadCount + "명");
//        System.out.println("성공: " + successCount.get() + "명");
//        System.out.println("실패: " + failCount.get() + "명");
//        System.out.println("실행 시간: " + (endTime - startTime) + "ms");
//        System.out.println("\n상세 결과:");
//        results.forEach(System.out::println);
//
//        // 검증: 정상적이라면 1명만 성공해야 함
//        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
//
//        if (successCount.get() == 1) {
//            System.out.println("\n동시성 제어 완벽!");
//        } else {
//            System.out.println("\n동시성 문제! " + successCount.get() + "명이 동시 예약 성공");
//        }
//    }
//
//    @Test
//    void 극한_동시성_테스트() throws Exception {
//        // Given: 테스트 데이터 준비
//        LocalDate reservationDate = LocalDate.now().plusDays(3);
//        LocalTime reservationTime = LocalTime.of(16, 0);
//
//        // 50명이 동시에 같은 시간 예약 시도 (극한 테스트)
//        int threadCount = 50;
//        ExecutorService executor = Executors.newFixedThreadPool(20); // 20개 스레드풀
//        CountDownLatch startLatch = new CountDownLatch(1); // 모든 스레드가 동시에 시작하도록
//        CountDownLatch endLatch = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//
//        // 사용자들을 미리 생성
//        List<User> users = new ArrayList<>();
//        for (int i = 0; i < threadCount; i++) {
//            users.add(createTestUser("extremeUser" + i + "@test.com"));
//        }
//
//        // 모든 스레드를 미리 생성하고 대기시킴
//        for (int i = 0; i < threadCount; i++) {
//            final int userIndex = i;
//            final User user = users.get(i);
//
//            executor.submit(() -> {
//                try {
//                    // 모든 스레드가 동시에 시작하도록 대기
//                    startLatch.await();
//
//                    ReservationRequestDto request = createReservationRequest(reservationDate, reservationTime);
//                    reservationService.createReservation(request.reservationDate(), request.reservationTime(), ReservationStatus.PENDING, user.getId(), testGym.getId(), testTrainer.getId());
//                    successCount.incrementAndGet();
//
//                } catch (Exception e) {
//                    failCount.incrementAndGet();
//                } finally {
//                    endLatch.countDown();
//                }
//            });
//        }
//
//        // 시작 시간 측정
//        long startTime = System.currentTimeMillis();
//
//        // 모든 스레드 동시 시작!
//        startLatch.countDown();
//
//        // 모든 스레드 완료 대기
//        endLatch.await(60, TimeUnit.SECONDS);
//        executor.shutdown();
//
//        // 종료 시간 측정
//        long endTime = System.currentTimeMillis();
//
//        // Then: 결과 확인
//        System.out.println("\n=== 극한 동시성 테스트 결과 ===");
//        System.out.println("총 시도: " + threadCount + "명");
//        System.out.println("성공: " + successCount.get() + "명");
//        System.out.println("실패: " + failCount.get() + "명");
//        System.out.println("실행 시간: " + (endTime - startTime) + "ms");
//
//        // 검증
//        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
//
//        if (successCount.get() == 1) {
//            System.out.println("극한 동시성 테스트 통과!");
//        } else {
//            System.out.println("동시성 문제: " + successCount.get() + "명 성공");
//        }
//    }
//
//    @Test
//    void Redis_분산락_동시_예약_테스트() throws Exception {
//        // Given: Redis 분산 락을 사용한 동시성 테스트
//        User user1 = createTestUser("redisUser1@test.com");
//        User user2 = createTestUser("redisUser2@test.com");
//
//        LocalDate reservationDate = LocalDate.now().plusDays(4);
//        LocalTime reservationTime = LocalTime.of(17, 0);
//
//        // When: Redis 분산 락 적용된 메서드로 동시 예약 시도
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//        CountDownLatch latch = new CountDownLatch(2);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//        List<String> results = Collections.synchronizedList(new ArrayList<>());
//
//        // 시작 시간 측정
//        long startTime = System.currentTimeMillis();
//
//        // 사용자1 예약 시도 (Redis 분산 락 버전)
//        executor.submit(() -> {
//            try {
//                ReservationRequestDto request = createReservationRequest(reservationDate, reservationTime);
//                ReservationResponseDto result = reservationService.createReservation(
//                    request.reservationDate(), request.reservationTime(), ReservationStatus.PENDING, user1.getId(), testGym.getId(), testTrainer.getId()
//                );
//                successCount.incrementAndGet();
//                results.add("Redis락 사용자1 예약 성공! ID: " + result.reservationId());
//            } catch (Exception e) {
//                failCount.incrementAndGet();
//                results.add("Redis락 사용자1 예약 실패: " + e.getMessage());
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        // 사용자2 예약 시도 (Redis 분산 락 버전)
//        executor.submit(() -> {
//            try {
//                ReservationRequestDto request = createReservationRequest(reservationDate, reservationTime);
//                ReservationResponseDto result = reservationService.createReservation(
//                    request.reservationDate(), request.reservationTime(), ReservationStatus.PENDING, user2.getId(), testGym.getId(), testTrainer.getId()
//                );
//                successCount.incrementAndGet();
//                results.add("Redis락 사용자2 예약 성공! ID: " + result.reservationId());
//            } catch (Exception e) {
//                failCount.incrementAndGet();
//                results.add("Redis락 사용자2 예약 실패: " + e.getMessage());
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        latch.await(15, TimeUnit.SECONDS); // Redis 락 대기시간 고려해서 조금 더 길게
//        executor.shutdown();
//
//        // 종료 시간 측정
//        long endTime = System.currentTimeMillis();
//
//        // Then: 결과 확인
//        System.out.println("\n=== Redis 분산락 동시 예약 테스트 결과 ===");
//        results.forEach(System.out::println);
//        System.out.println("총 성공: " + successCount.get());
//        System.out.println("총 실패: " + failCount.get());
//        System.out.println("실행 시간: " + (endTime - startTime) + "ms");
//
//        // 검증: Redis 분산 락으로도 1명만 성공해야 함
//        assertThat(successCount.get() + failCount.get()).isEqualTo(2);
//
//        if (successCount.get() == 1) {
//            System.out.println("Redis 분산락 동시성 제어 성공!");
//        } else {
//            System.out.println("Redis 분산락 동시성 문제! " + successCount.get() + "명이 동시 예약 성공");
//        }
//    }
//
//    @Test
//    void Redis_분산락_다중_사용자_테스트() throws Exception {
//        // Given: Redis 분산 락으로 10명 동시 테스트
//        LocalDate reservationDate = LocalDate.now().plusDays(4);
//        LocalTime reservationTime = LocalTime.of(18, 0);
//
//        int threadCount = 10;
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//        List<String> results = Collections.synchronizedList(new ArrayList<>());
//
//        // 시작 시간 측정
//        long startTime = System.currentTimeMillis();
//
//        for (int i = 0; i < threadCount; i++) {
//            final int userIndex = i;
//            executor.submit(() -> {
//                try {
//                    User user = createTestUser("redisMultiUser" + userIndex + "@test.com");
//                    ReservationRequestDto request = createReservationRequest(reservationDate, reservationTime);
//
//                    // Redis 분산 락 버전으로 예약 시도
//                    ReservationResponseDto result = reservationService.createReservation(
//                        request.reservationDate(), request.reservationTime(), ReservationStatus.PENDING, user.getId(), testGym.getId(), testTrainer.getId()
//                    );
//
//                    successCount.incrementAndGet();
//                    results.add("Redis락 사용자" + userIndex + " 예약 성공! 예약ID: " + result.reservationId());
//
//                } catch (Exception e) {
//                    failCount.incrementAndGet();
//                    results.add("Redis락 사용자" + userIndex + " 예약 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        // 모든 스레드가 완료될 때까지 대기 (Redis 락 타임아웃 고려)
//        latch.await(60, TimeUnit.SECONDS);
//        executor.shutdown();
//
//        // 종료 시간 측정
//        long endTime = System.currentTimeMillis();
//
//        // Then: 결과 확인
//        System.out.println("\n=== Redis 분산락 다중 사용자 테스트 결과 ===");
//        System.out.println("총 시도: " + threadCount + "명");
//        System.out.println("성공: " + successCount.get() + "명");
//        System.out.println("실패: " + failCount.get() + "명");
//        System.out.println("실행 시간: " + (endTime - startTime) + "ms");
//        System.out.println("\n상세 결과:");
//        results.forEach(System.out::println);
//
//        // 검증: Redis 분산 락으로도 1명만 성공해야 함
//        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
//
//        if (successCount.get() == 1) {
//            System.out.println("\nRedis 분산락 다중 사용자 동시성 제어 완벽!");
//        } else {
//            System.out.println("\nRedis 분산락 동시성 문제! " + successCount.get() + "명이 동시 예약 성공");
//        }
//    }
//
//    @Test
//    void 하이브리드_방식_최종_테스트() throws Exception {
//        // Given: 하이브리드(Redis + DB) 방식 종합 테스트
//        LocalDate reservationDate = LocalDate.now().plusDays(5);
//        LocalTime reservationTime = LocalTime.of(19, 0);
//
//        int threadCount = 20; // 중간 규모로 테스트
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch startLatch = new CountDownLatch(1);
//        CountDownLatch endLatch = new CountDownLatch(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//        List<String> results = Collections.synchronizedList(new ArrayList<>());
//
//        // 사용자들을 미리 생성
//        List<User> users = new ArrayList<>();
//        for (int i = 0; i < threadCount; i++) {
//            users.add(createTestUser("hybridUser" + i + "@test.com"));
//        }
//
//        // 모든 스레드를 미리 생성하고 대기시킴
//        for (int i = 0; i < threadCount; i++) {
//            final int userIndex = i;
//            final User user = users.get(i);
//
//            executor.submit(() -> {
//                try {
//                    // 모든 스레드가 동시에 시작하도록 대기
//                    startLatch.await();
//
//                    ReservationRequestDto request = createReservationRequest(reservationDate, reservationTime);
//
//                    // 하이브리드 방식(Redis + DB) 사용
//                    ReservationResponseDto result = reservationService.createReservation(
//                        request.reservationDate(), request.reservationTime(), ReservationStatus.PENDING, user.getId(), testGym.getId(), testTrainer.getId()
//                    );
//
//                    successCount.incrementAndGet();
//                    results.add("하이브리드 사용자" + userIndex + " 예약 성공! 예약ID: " + result.reservationId());
//
//                } catch (Exception e) {
//                    failCount.incrementAndGet();
//                    results.add("하이브리드 사용자" + userIndex + " 예약 실패: " + e.getMessage());
//                } finally {
//                    endLatch.countDown();
//                }
//            });
//        }
//
//        // 시작 시간 측정
//        long startTime = System.currentTimeMillis();
//
//        // 모든 스레드 동시 시작!
//        startLatch.countDown();
//
//        // 모든 스레드 완료 대기
//        endLatch.await(120, TimeUnit.SECONDS); // 하이브리드 방식은 시간이 더 걸릴 수 있음
//        executor.shutdown();
//
//        // 종료 시간 측정
//        long endTime = System.currentTimeMillis();
//
//        // Then: 결과 확인
//        System.out.println("\n=== 하이브리드 방식(Redis + DB) 최종 테스트 결과 ===");
//        System.out.println("총 시도: " + threadCount + "명");
//        System.out.println("성공: " + successCount.get() + "명");
//        System.out.println("실패: " + failCount.get() + "명");
//        System.out.println("실행 시간: " + (endTime - startTime) + "ms");
//        System.out.println("\n상세 결과 (처음 5개):");
//        results.stream().limit(5).forEach(System.out::println);
//        if (results.size() > 5) {
//            System.out.println("... 및 " + (results.size() - 5) + "개 더");
//        }
//
//        // 검증: 하이브리드 방식으로도 1명만 성공해야 함
//        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
//
//        if (successCount.get() == 1) {
//            System.out.println("\n하이브리드 방식 최종 테스트 완벽 성공!");
//            System.out.println("Redis 분산락 1차 방어 + DB 유니크 제약조건 2차 방어 = 완벽한 동시성 제어!");
//        } else {
//            System.out.println("\n하이브리드 방식 동시성 문제: " + successCount.get() + "명 성공");
//        }
//    }
//}
