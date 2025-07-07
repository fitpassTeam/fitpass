package org.example.fitpass.domain.likes.entity;

import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@email.com", "testUser", "LOCAL");
        // 리플렉션으로 id 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Like 엔티티 생성 - 생성자 테스트")
    void Like_생성자_테스트() {
        // given
        LikeType likeType = LikeType.GYM;
        Long targetId = 1L;

        // when
        Like like = new Like(user, likeType, targetId);

        // then
        assertThat(like.getUser()).isEqualTo(user);
        assertThat(like.getLikeType()).isEqualTo(likeType);
        assertThat(like.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Like 엔티티 생성 - of 정적 팩토리 메서드 테스트")
    void Like_of_정적_팩토리_메서드_테스트() {
        // given
        LikeType likeType = LikeType.POST;
        Long targetId = 2L;

        // when
        Like like = Like.of(user, likeType, targetId);

        // then
        assertThat(like.getUser()).isEqualTo(user);
        assertThat(like.getLikeType()).isEqualTo(likeType);
        assertThat(like.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("GYM 타입 Like 생성")
    void GYM_타입_Like_생성() {
        // given
        Long gymId = 5L;

        // when
        Like gymLike = Like.of(user, LikeType.GYM, gymId);

        // then
        assertThat(gymLike.getUser()).isEqualTo(user);
        assertThat(gymLike.getLikeType()).isEqualTo(LikeType.GYM);
        assertThat(gymLike.getTargetId()).isEqualTo(gymId);
    }

    @Test
    @DisplayName("POST 타입 Like 생성")
    void POST_타입_Like_생성() {
        // given
        Long postId = 10L;

        // when
        Like postLike = Like.of(user, LikeType.POST, postId);

        // then
        assertThat(postLike.getUser()).isEqualTo(user);
        assertThat(postLike.getLikeType()).isEqualTo(LikeType.POST);
        assertThat(postLike.getTargetId()).isEqualTo(postId);
    }

    @Test
    @DisplayName("같은 사용자가 다른 대상에 좋아요 - 각각 다른 Like 객체")
    void 같은_사용자_다른_대상_좋아요() {
        // given
        Long gymId = 1L;
        Long postId = 2L;

        // when
        Like gymLike = Like.of(user, LikeType.GYM, gymId);
        Like postLike = Like.of(user, LikeType.POST, postId);

        // then
        assertThat(gymLike.getUser()).isEqualTo(postLike.getUser());
        assertThat(gymLike.getLikeType()).isNotEqualTo(postLike.getLikeType());
        assertThat(gymLike.getTargetId()).isNotEqualTo(postLike.getTargetId());
    }

    @Test
    @DisplayName("NoArgsConstructor 테스트")
    void NoArgsConstructor_테스트() {
        // when
        Like like = new Like();

        // then
        assertThat(like.getUser()).isNull();
        assertThat(like.getLikeType()).isNull();
        assertThat(like.getTargetId()).isNull();
        assertThat(like.getId()).isNull();
    }
}
