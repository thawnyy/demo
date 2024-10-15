package com.kakaobank.practice.searchkeyword.service.impl;

import com.kakaobank.practice.searchkeyword.dto.PopularSearchKeywordDto;
import com.kakaobank.practice.searchkeyword.dto.SearchKeywordResponseDto;
import com.kakaobank.practice.searchkeyword.repository.SearchKeywordHistoryRepository;
import com.kakaobank.practice.searchkeyword.repository.SearchKeywordRepository;
import com.kakaobank.practice.searchkeyword.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchKeywordServiceImpl implements SearchKeywordService {

    private final SearchKeywordRepository searchKeywordRepository;
    private final SearchKeywordHistoryRepository searchKeywordHistoryRepository;

    @Override
    public SearchKeywordResponseDto findTop10SearchKeyword() {
        // count 내림차순으로 10개 가지고 오기
        return new SearchKeywordResponseDto(searchKeywordRepository.findTop10ByOrderByCountDesc().stream()
                .map(PopularSearchKeywordDto::from)
                .collect(Collectors.toList()));
    }
}
