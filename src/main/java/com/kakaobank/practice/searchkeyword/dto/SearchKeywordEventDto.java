package com.kakaobank.practice.searchkeyword.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@ToString
public class SearchKeywordEventDto {
    protected String keyword;
    protected LocalDateTime eventTime;

    public SearchKeywordEventDto(String keyword, LocalDateTime eventTime) {
        this.keyword = keyword;
        this.eventTime = eventTime;
    }

}
