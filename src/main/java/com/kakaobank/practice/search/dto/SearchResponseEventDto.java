package com.kakaobank.practice.search.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SearchResponseEventDto(String key, String keyword, List<SearchResultDto> results, int durationMinutes, LocalDateTime eventTime) {

}