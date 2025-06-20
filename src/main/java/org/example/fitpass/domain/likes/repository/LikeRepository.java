package org.example.fitpass.domain.likes.repository;

import java.util.Optional;
import java.util.Set;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.likes.entity.Like;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndTargetId(User user, Long targetId);

    @Query("SELECT l.targetId FROM likes l WHERE l.user.id = :userId AND l.likeType = :likeType")
    Set<Long> findTargetIdsByUserIdAndLikeType(@Param("userId") Long userId, @Param("likeType") LikeType likeType);
}
