package org.example.fitpass.domain.search.repository;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.domain.search.entity.SearchKeywordGym;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchGymRepository extends JpaRepository<SearchKeywordGym, Long>{

    Optional<SearchKeywordGym> findByKeyword (String keyword);

    List<SearchKeywordGym> findTop5ByOrderByCountDesc();
}