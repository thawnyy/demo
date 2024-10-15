package com.kakaobank.practice.searchkeyword.adapter;

import com.kakaobank.practice.searchkeyword.dto.SearchKeywordResponseDto;
import com.kakaobank.practice.searchkeyword.facade.SearchKeywordFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SearchKeywordController {
    private final SearchKeywordFacade searchKeywordFacade;

    @GetMapping("/api/popular-search-keywords")
    public SearchKeywordResponseDto popularSearchKeyword() {
        return searchKeywordFacade.findTop10SearchKeyword();
    }
}
