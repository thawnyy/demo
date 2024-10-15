package com.kakaobank.practice.externalmodule.naver.service;

import com.kakaobank.practice.externalmodule.naver.dto.NaverMapSearchResponseDto;

public interface NaverMapSearchService {
    NaverMapSearchResponseDto search(String keyword);
}
