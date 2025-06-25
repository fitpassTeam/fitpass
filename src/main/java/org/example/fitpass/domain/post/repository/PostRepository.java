package org.example.fitpass.domain.post.repository;

import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findById(Long post);

    default Post findByIdOrElseThrow(Long postId) {
        Post post = findById(postId).orElseThrow(
                () -> new BaseException(ExceptionCode.POST_NOT_FOUND));
        return post;
    }

    // GENERAL 게시물 페이징 조회
    Page<Post> findByGymIdAndPostType(Long gymId, PostType postType, Pageable pageable);

    // NOTICE 게시물 리스트 조회 생성시간 내림차순
    @Query("SELECT p FROM Post p WHERE p.gym.id = :gymId AND p.postType = :postType ORDER BY p.createdAt DESC")
    List<Post> findByGymIdAndPostType(@Param("gymId")Long gymId, @Param("postType") PostType postType);

    @Query("SELECT p FROM Post p WHERE p.postStatus <> 'DELETED' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);

}
