package com.kakaobank.practice.searchkeyword.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@ToString
@Table(
        indexes = {
                @Index(name = "search_keyword_keyword_idx", columnList = "keyword")
        }
)
public class SearchKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String keyword;

    private Long count;

    private SearchKeyword(String keyword, Long count) {
        this.keyword = keyword;
        this.count = count;
    }

    public SearchKeyword() {

    }

    public static SearchKeyword of(String keyword, Long count) {
        return new SearchKeyword(keyword, count);
    }

    public void increaseCount() {
        this.count++;
    }

    public void changeCount(Long count) {
        this.count = count;
    }

}
