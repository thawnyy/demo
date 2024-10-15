package com.kakaobank.practice.searchkeyword.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@Table(
        indexes = {
                @Index(name = "search_keyword_history_keyword_idx", columnList = "keyword")
        }
)
public class SearchKeywordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String keyword;

    @CreatedDate
    private LocalDateTime createdDateTime;

    private SearchKeywordHistory(String keyword) {
        this.keyword = keyword;
    }

    public SearchKeywordHistory() {

    }

    public static SearchKeywordHistory of(String keyword) {
        return new SearchKeywordHistory(keyword);
    }

}
