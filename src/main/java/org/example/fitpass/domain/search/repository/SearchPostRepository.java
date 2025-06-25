package org.example.fitpass.domain.search.repository;

import org.example.fitpass.domain.search.entity.SearchKeywordPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchPostRepository extends JpaRepository<SearchKeywordPost, Long> {

    Optional<SearchKeywordPost> findByKeyword (String keyword);

}