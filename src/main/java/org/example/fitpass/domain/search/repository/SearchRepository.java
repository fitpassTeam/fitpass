package org.example.fitpass.domain.search.repository;

import org.example.fitpass.domain.search.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchRepository extends JpaRepository<SearchKeyword, Long> {

    Optional<SearchKeyword> findByKeyword (String keyword);


}
