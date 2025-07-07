package org.example.fitpass.domain.search.repository;

import java.util.Optional;
import org.example.fitpass.domain.search.entity.SearchKeywordPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchPostRepository extends JpaRepository<SearchKeywordPost, Long> {

    Optional<SearchKeywordPost> findByKeyword (String keyword);

}