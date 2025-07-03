package org.example.fitpass.domain.search.repository;

import java.util.Optional;
import org.example.fitpass.domain.search.entity.SearchKeywordTrainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchTrainerRepository extends JpaRepository<SearchKeywordTrainer, Long> {

    Optional<SearchKeywordTrainer> findByKeyword (String keyword);

}