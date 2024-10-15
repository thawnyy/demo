package com.kakaobank.practice.searchkeyword.repository;

import com.kakaobank.practice.searchkeyword.domain.SearchKeyword;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SearchKeyword> findByKeyword(String keyword);

    List<SearchKeyword> findTop10ByOrderByCountDesc();
}
