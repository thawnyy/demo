package com.kakaobank.practice.searchkeyword.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class SearchKeywordResponseDto {
    private List<PopularSearchKeywordDto> keywords;

    public SearchKeywordResponseDto(List<PopularSearchKeywordDto> keywords) {
        this.keywords = keywords;
    }
}
