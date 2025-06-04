package org.example.fitpass.domain.post.repository;

import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findById(Long post);

    default Post findByIdOrElseThrow(Long postId) {
        Post post = findById(postId).orElseThrow(
                () -> new BaseException(ExceptionCode.POST_NOT_FOUND));
        return post;
    }


    Page<Post> findAll(Pageable pageable);
}
