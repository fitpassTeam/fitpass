package org.example.fitpass.domain.post.repository;

import org.example.fitpass.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {

    Optional<Post> findById (Long post);

    default Post findByIdOrElseThrow(Long postId) {
        Post post = findById(postId).orElseThrow(
                () -> new RuntimeException("존재하지 않은 게시물 id입니다."));
        return post;
    }
}
