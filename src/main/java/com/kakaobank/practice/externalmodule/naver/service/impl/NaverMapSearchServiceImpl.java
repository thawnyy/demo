package com.kakaobank.practice.externalmodule.naver.service.impl;

import com.kakaobank.practice.externalmodule.naver.NaverSearchProperties;
import com.kakaobank.practice.externalmodule.naver.constant.UriConstants;
import com.kakaobank.practice.externalmodule.naver.dto.NaverMapSearchResponseDto;
import com.kakaobank.practice.externalmodule.naver.service.NaverMapSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverMapSearchServiceImpl implements NaverMapSearchService {

    private final NaverSearchProperties properties;
    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;

    @Override
    public NaverMapSearchResponseDto search(String keyword) {
        return retryTemplate.execute(context -> {
            try {
                String url = properties.getApi().getHost() + UriConstants.NAVER_MAP_API_URI + keyword;

                HttpHeaders httpHeaders = getHttpHeaders();
                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(httpHeaders);

                NaverMapSearchResponseDto response = restTemplate.exchange(url, HttpMethod.GET, request, NaverMapSearchResponseDto.class).getBody();
                return response;
            } catch (Exception ex) {
                log.error("[NAVER-MAP-SEARCH] api call ERROR", ex);
                throw ex;
            }
        }, context -> {
            log.error("[NAVER-MAP-SEARCH] api call 최종 ERROR");
            throw new RuntimeException("NAVER API call failed after retries");
        });
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("X-Naver-Client-Id", properties.getApi().getClientId());
        httpHeaders.set("X-Naver-Client-Secret", properties.getApi().getClientSecret());
        httpHeaders.add("Accept", "*/*");
        return httpHeaders;
    }
}
