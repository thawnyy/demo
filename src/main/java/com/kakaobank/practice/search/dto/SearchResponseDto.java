package com.kakaobank.practice.search.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchResponseDto {
    private String keyword;
    private List<SearchResultDto> results;

    public SearchResponseDto(String keyword, List<SearchResultDto> results) {
        this.keyword = keyword;
        this.results = results;
    }
}