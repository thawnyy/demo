package com.kakaobank.practice.search.adapter;

import com.kakaobank.practice.search.dto.SearchResponseDto;
import com.kakaobank.practice.search.facade.SearchFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchFacade searchFacade;

    @GetMapping("/api/search")
    public SearchResponseDto search(String keyword) {
        log.info("[SearchController] search - keyword: {}", keyword);
        validationCheck(keyword);
        return searchFacade.search(keyword);
    }

    private static void validationCheck(String keyword) {
        if(StringUtils.isEmpty(keyword) || StringUtils.isEmpty(keyword.replaceAll(" ", ""))) {
            throw new IllegalArgumentException("검색어를 확인해주세요.");
        }
        if(keyword.length() < 2) {
            throw new IllegalArgumentException("검색어는 2글자 이상 입력해주세요.");
        }
        if(keyword.length() > 10) {
            throw new IllegalArgumentException("검색어는 10글자 이하로 입력해주세요.");
        }
    }
}


