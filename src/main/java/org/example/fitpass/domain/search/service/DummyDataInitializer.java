package org.example.fitpass.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {

    private final GymRepository gymRepository;
    private final UserRepository userRepository; // 소유자 정보 필요 시

    @Override
    public void run(String... args) throws Exception {
        if (gymRepository.count() > 0) {
            return; // 이미 데이터가 있다면 중단
        }

        User defaultUser = userRepository.findById(1L)
            .orElseThrow(() -> new RuntimeException("기본 유저가 없습니다."));

        List<Gym> gyms = new ArrayList<>();
        for (int i = 1; i <= 100000; i++) {
            String name = GymNameGenerator.generate();  // 다양화된 이름
            String number = "010-0000-" + String.format("%04d", i);
            String content = "이곳은 " + name + "입니다.";
            String address = "서울시 강남구 테크노 " + i + "로";
            LocalTime open = LocalTime.of(6, 0);
            LocalTime close = LocalTime.of(23, 0);

            Gym gym = Gym.of(
                new ArrayList<>(),
                name,
                number,
                content,
                address,
                open,
                close,
                defaultUser
            );

            gyms.add(gym);

            if (gyms.size() % 1000 == 0) {
                gymRepository.saveAll(gyms);
                gyms.clear();
                System.out.println(i + "개 생성 완료");
            }
        }

        // 나머지 데이터 저장
        if (!gyms.isEmpty()) {
            gymRepository.saveAll(gyms);
            System.out.println("최종 저장 완료");
        }
    }
}
