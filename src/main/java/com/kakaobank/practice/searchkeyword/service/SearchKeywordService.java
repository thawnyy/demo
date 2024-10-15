package com.kakaobank.practice.searchkeyword.service;

import com.kakaobank.practice.searchkeyword.dto.SearchKeywordResponseDto;

public interface SearchKeywordService {
    SearchKeywordResponseDto findTop10SearchKeyword();
}
