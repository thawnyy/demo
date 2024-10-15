package com.kakaobank.practice.searchkeyword.adapter;

import com.kakaobank.practice.commons.lock.support.DistributionLock;
import com.kakaobank.practice.searchkeyword.domain.SearchKeyword;
import com.kakaobank.practice.searchkeyword.domain.SearchKeywordHistory;
import com.kakaobank.practice.searchkeyword.dto.SearchKeywordEventDto;
import com.kakaobank.practice.searchkeyword.repository.SearchKeywordHistoryRepository;
import com.kakaobank.practice.searchkeyword.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchKeywordEventHandler {

    private final SearchKeywordRepository searchKeywordRepository;
    private final SearchKeywordHistoryRepository searchKeywordHistoryRepository;

    @DistributionLock(
            prefix = "SEARCH-KEYWORD",
            lockKey = "#event.keyword",
            waitTime = 3000,
            leaseTime = 50000
    )
    @EventListener
    @Transactional
    public void handle(SearchKeywordEventDto event) {
        log.info("[SearchKeywordEventHandler] handle - event: {}", event);
        String keyword = event.getKeyword();
        searchKeywordHistoryRepository.save(SearchKeywordHistory.of(keyword));

        long keywordCount = searchKeywordHistoryRepository.countByKeyword(keyword);
        var searchKeywordOptional = searchKeywordRepository.findByKeyword(keyword);

        log.info("[SearchKeywordEventHandler] keywordCount: {}", keywordCount);
        log.info("[SearchKeywordEventHandler] searchKeywordOptional: {}", searchKeywordOptional);

        if(searchKeywordOptional.isPresent() && searchKeywordOptional.get().getCount() < keywordCount) {
            searchKeywordOptional.get().changeCount(keywordCount);
        } else {
            searchKeywordRepository.save(SearchKeyword.of(keyword, keywordCount));
        }

    }
}
