package com.kakaobank.practice.searchkeyword.dto;

import com.kakaobank.practice.searchkeyword.domain.SearchKeyword;
import lombok.Getter;

@Getter
public class PopularSearchKeywordDto {
    private final String keyword;
    private final long count;

    public PopularSearchKeywordDto(String keyword, Long count) {
        this.keyword = keyword;
        this.count = count;
    }

    public static PopularSearchKeywordDto from(SearchKeyword searchKeyword) {
        return new PopularSearchKeywordDto(searchKeyword.getKeyword(), searchKeyword.getCount());
    }
}
