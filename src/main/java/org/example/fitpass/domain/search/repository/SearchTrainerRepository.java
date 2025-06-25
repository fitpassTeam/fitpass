package org.example.fitpass.domain.search.repository;

import org.example.fitpass.domain.search.entity.SearchKeywordTrainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchTrainerRepository extends JpaRepository<SearchKeywordTrainer, Long> {

    Optional<SearchKeywordTrainer> findByKeyword (String keyword);

}