package com.kakaobank.practice.searchkeyword.repository;

import com.kakaobank.practice.searchkeyword.domain.SearchKeywordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchKeywordHistoryRepository extends JpaRepository<SearchKeywordHistory, Long> {
    Long countByKeyword(String keyword);
}
