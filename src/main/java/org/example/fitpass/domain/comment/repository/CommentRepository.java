package org.example.fitpass.domain.comment.repository;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.comment.entity.Comment;
import org.example.fitpass.domain.gym.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findById(Long gymId);

    default Comment findByIdOrElseThrow(Long parentId){
        return findById(parentId).orElseThrow(
                () -> new BaseException(ExceptionCode.COMMENT_NOT_FOUND));
    };

    List<Comment> findByPostIdAndParentIsNull(Long postId);

    long countByPostId(Long postId);
}
