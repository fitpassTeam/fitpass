package org.example.fitpass.domain.search.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;

@Entity
@Getter
@Table(name = "search_keyword_gym")
@NoArgsConstructor

public class SearchKeywordGym extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "검색어 ID", example = "1")
    private Long id;

    @Column(unique = true)
    @Schema(description = "검색어", example = "Fitpass헬스장")
    private String keyword;

    @Column(nullable = false)
    @Schema(description = "총계", example = "13")
    private int count;

    public SearchKeywordGym(String keyword) {

        this.keyword = keyword;
        this.count = 1;

    }

    public void increaseCount() {

        this.count++;

    }
}
