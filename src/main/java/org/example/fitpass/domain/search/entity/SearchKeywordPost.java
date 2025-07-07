package org.example.fitpass.domain.search.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.entity.BaseEntity;

@Entity
@Getter
@Table(name = "search_keyword_Post")
@NoArgsConstructor

public class SearchKeywordPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "검색어 ID", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "검색어", example = "이 식단 정말 최고에요")
    private String keyword;

    @Column(nullable = false)
    @Schema(description = "총계", example = "188")
    private int count;

    public SearchKeywordPost(String keyword) {

        this.keyword = keyword;
        this.count = 1;

    }

    public void increaseCount() {

        this.count++;

    }
}