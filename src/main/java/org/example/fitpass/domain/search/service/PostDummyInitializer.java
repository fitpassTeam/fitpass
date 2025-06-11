//package org.example.fitpass.domain.search.service;
//
//import lombok.RequiredArgsConstructor;
//import org.example.fitpass.domain.gym.entity.Gym;
//import org.example.fitpass.domain.gym.repository.GymRepository;
//import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
//import org.example.fitpass.domain.post.entity.Post;
//import org.example.fitpass.domain.post.enums.PostStatus;
//import org.example.fitpass.domain.post.enums.PostType;
//import org.example.fitpass.domain.post.repository.PostRepository;
//import org.example.fitpass.domain.user.entity.User;
//import org.example.fitpass.domain.user.repository.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//@Component
//@RequiredArgsConstructor
//public class PostDummyInitializer implements CommandLineRunner {
//
//
//    private final PostRepository postRepository;
//    private final UserRepository userRepository;
//    private final GymRepository gymRepository;
//
//    private static final Random random = new Random();
//
//    private static final String[] TITLES = {
//            "운동 루틴", "식단 공개", "공지사항", "운동 꿀팁", "후기", "질문 있어요", "정보 공유", "챌린지 모집"
//    };
//
//    private static final String[] CONTENTS = {
//            "오늘은 하체 루틴을 소화했습니다.", "단백질 보충에 대해 알려드릴게요.", "이번 주는 휴무입니다.",
//            "질문 있습니다! 데드리프트 자세 점검 부탁드려요.", "헬스장 분위기 최고예요!", "3개월 후기 공유합니다.",
//            "새로운 챌린지 참가자 모집 중입니다!", "다이어트는 역시 식단이 중요하네요."
//    };
//
//    @Override
//
//    public void run(String... args) throws Exception {
//        if (postRepository.count() > 0) return;
//
//        User user = userRepository.findById(1L)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        Gym gym = gymRepository.findById(1L)
//                .orElseThrow(() -> new RuntimeException("Gym not found"));
//
//        List<Post> posts = new ArrayList<>();
//
//        for (int i = 1; i <= 100000; i++) {
//            PostCreateRequestDto dto = createRandomPostDto(i);
//
//            Post post = new Post(
//                    dto.getStatus(),
//                    dto.getPostType(),
//                    dto.getPostImage(),
//                    dto.getTitle(),
//                    dto.getContent(),
//                    user,
//                    gym
//            );
//
//            posts.add(post);
//
//            if (posts.size() % 1000 == 0) {
//                postRepository.saveAll(posts);
//                posts.clear();
//                System.out.println(i + "개 생성 완료");
//            }
//        }
//
//        if (!posts.isEmpty()) {
//            postRepository.saveAll(posts);
//        }
//
//        System.out.println("📦 PostCreateRequestDto 기반 더미 데이터 10,000개 생성 완료!");
//    }
//
//    private PostCreateRequestDto createRandomPostDto(int index) {
//        String title = getRandom(TITLES) + " #" + index;
//        String content = getRandom(CONTENTS);
//        String image = "https://cdn.example.com/images/" + (index % 10) + ".jpg";
//
//        // 90% 확률로 ACTIVE, 10% 확률로 DELETED 상태 부여
//        PostStatus status = random.nextDouble() < 0.9 ? PostStatus.ACTIVE : PostStatus.DELETED;
//
//        // 공지는 20번째 글마다 한 번씩 생성
//        PostType type = (index % 20 == 0) ? PostType.NOTICE : PostType.GENERAL;
//
//        return new PostCreateRequestDto(
//                status,
//                type,
//                title,
//                content,
//                image
//        );
//    }
//
//    private String getRandom(String[] array) {
//        return array[random.nextInt(array.length)];
//    }
//}