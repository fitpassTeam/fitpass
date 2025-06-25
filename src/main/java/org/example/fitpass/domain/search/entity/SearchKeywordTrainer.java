package org.example.fitpass.domain.search.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;

@Entity
@Getter
@Table(name = "search_keyword_Trainer")
@NoArgsConstructor

public class SearchKeywordTrainer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyword;

    @Column(nullable = false)
    private int count;

    public SearchKeywordTrainer(String keyword) {

        this.keyword = keyword;
        this.count = 1;

    }

    public void increaseCount() {

        this.count++;

    }
}