package com.kakaobank.practice.searchkeyword.facade;

import com.kakaobank.practice.searchkeyword.dto.SearchKeywordResponseDto;
import com.kakaobank.practice.searchkeyword.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchKeywordFacade {
    private final SearchKeywordService searchKeywordService;
    public SearchKeywordResponseDto findTop10SearchKeyword() {
        // count 내림차순으로 10개 가지고 오기
        return searchKeywordService.findTop10SearchKeyword();
    }
}
