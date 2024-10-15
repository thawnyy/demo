package com.kakaobank.practice.search.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaobank.practice.externalmodule.kakao.dto.Document;
import com.kakaobank.practice.externalmodule.kakao.dto.KakaoMapSearchResponseDto;
import com.kakaobank.practice.externalmodule.kakao.service.KakaoMapSearchService;
import com.kakaobank.practice.externalmodule.naver.dto.Item;
import com.kakaobank.practice.externalmodule.naver.dto.NaverMapSearchResponseDto;
import com.kakaobank.practice.externalmodule.naver.service.NaverMapSearchService;
import com.kakaobank.practice.search.dto.SearchResponseEventDto;
import com.kakaobank.practice.search.dto.SearchResponseDto;
import com.kakaobank.practice.search.dto.SearchResultDto;
import com.kakaobank.practice.searchkeyword.dto.SearchKeywordEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Collections.emptyList;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchFacade {
    private final KakaoMapSearchService kakaoMapSearchService;
    private final NaverMapSearchService naverMapSearchService;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String RESULT = "RESULT";
    private static final String KAKAO = "KAKAO";
    private static final String NAVER = "NAVER";

    public SearchResponseDto search(String keyword) {
        log.info("[SearchFacade] search - keyword: {}", keyword);

        // 검색어를 application event로 publish
        publishEventForSaveSearchKeyword(keyword);

        // redis에 검색 결과가 있는지 확인
        SearchResponseDto redisResponse = findSearchResultFromRedis(keyword, RESULT);
        if (redisResponse != null) return redisResponse;

        // 검색 엔진 별 검색 결과 조회
        Map<String, List<SearchResultDto>> searchResults = findFromSearchEngines(keyword);

        // 검색 결과를 노출 정책에 맞게 조합
        SearchResponseDto response = transformSearchResults(keyword, searchResults);

        // 검색 결과를 application event로 publish
        publishEventForSaveSearchResult(new SearchResponseDto(keyword, searchResults.get(KAKAO)), KAKAO);
        publishEventForSaveSearchResult(new SearchResponseDto(keyword, searchResults.get(NAVER)), NAVER);
        publishEventForSaveSearchResult(response, RESULT);

        return response;
    }

    private Map<String, List<SearchResultDto>> findFromSearchEngines(String keyword) {
        Map<String, List<SearchResultDto>> searchResults = new HashMap<>();
        searchResults.put(KAKAO, findFromKakao(keyword));
        searchResults.put(NAVER, findFromNaver(keyword));

        return searchResults;
    }

    private List<SearchResultDto> findFromKakao(String keyword) {

        List<SearchResultDto> kakaoResults = emptyList();
        try {
            KakaoMapSearchResponseDto kakaoMapSearchResponseDto = kakaoMapSearchService.search(keyword);
            kakaoResults = getSearchResultDtosFromKakaoSearch(kakaoMapSearchResponseDto);
        } catch (Exception e) {
            log.error("[SearchFacade] search - error: {}", e.getMessage());
            SearchResponseDto kakaoResponse = findSearchResultFromRedis(keyword, KAKAO);
            if(kakaoResponse != null) {
                kakaoResults = kakaoResponse.getResults();
            }
        }
        return kakaoResults;
    }
    private List<SearchResultDto> findFromNaver(String keyword) {
        List<SearchResultDto> naverResults = emptyList();
        try {
            NaverMapSearchResponseDto naverMapSearchResponseDto = naverMapSearchService.search(keyword);
            naverResults = getSearchResultDtosFromNaverSearch(naverMapSearchResponseDto);
        } catch (Exception e) {
            log.error("[SearchFacade] search - error: {}", e.getMessage());
            SearchResponseDto naverResponse = findSearchResultFromRedis(keyword, NAVER);
            if(naverResponse != null) {
                naverResults = naverResponse.getResults();
            }
        }
        log.info("[SearchFacade] naverResults: {}", naverResults);
        return naverResults;
    }

    private SearchResponseDto findSearchResultFromRedis(String keyword, String type) {
        log.info("[SearchFacade] findSearchResultFromRedis - keyword: {}", keyword);

        String responseStr = redisTemplate.opsForValue().get(getRedisKey(keyword, type));
        log.info("[SearchFacade] findSearchResultFromRedis - responseStr: {}", responseStr);
        if(responseStr == null) return null;

        try {
            return objectMapper.readValue(responseStr, SearchResponseDto.class);
        } catch (IOException e) {
            log.error("[SearchFacade] findSearchResultFromRedis - error: {}", e.getMessage());
            return null;
        }
    }

    // persentation 조건에 맞게 response를 만들어서 return
    private SearchResponseDto transformSearchResults(String keyword, Map<String, List<SearchResultDto>> searchResults) {

        List<SearchResultDto> kakaoResults = searchResults.get(KAKAO);
        List<SearchResultDto> naverResults = searchResults.get(NAVER);

        // 결과를 저장할 리스트와 중복 체크를 위한 Set
        List<SearchResultDto> responseList = new ArrayList<>();
        Set<String> existName = new HashSet<>();

        for (SearchResultDto result : kakaoResults) {
            if (result != null) {
                responseList.add(result);
                existName.add(removeHtmlTagAndBlank(result.getPlaceName()));
            }
        }

        for (SearchResultDto result : naverResults) {
            if (result != null && existName.add(removeHtmlTagAndBlank(result.getPlaceName()))) {
                responseList.add(result);
            }
        }

        // 정렬: KAKAO와 NAVER에 모두 있는 경우가 가장 우선
        responseList.sort((r1, r2) -> {
            boolean inA1 = kakaoResults.contains(r1);
            boolean inA2 = kakaoResults.contains(r2);
            boolean inB1 = naverResults.contains(r1);
            boolean inB2 = naverResults.contains(r2);

            // 조건 1: KAKAO와 NAVER에 모두 있는 경우
            if (inA1 && inB1 && inA2 && inB2) return 0; // 순서 유지
            if (inA1 && inB1) return -1; // r1이 KAKAO와 NAVER에 모두 있으면 r1 우선
            if (inA2 && inB2) return 1; // r2가 KAKAO와 NAVER에 모두 있으면 r2 우선

            // 조건 2: KAKAO에만 있는 경우
            if (inA1 && !inA2) return -1; // r1이 KAKAO에 있으면 r1 우선
            if (!inA1 && inA2) return 1; // r2가 KAKAO에 있으면 r2 우선

            // 둘 다 KAKAO에 없고 NAVER에만 있는 경우는 순서 유지
            return 0;
        });
        log.info("[SearchFacade] responseList: {}", responseList);

        return new SearchResponseDto(keyword, responseList);
    }

    private static List<SearchResultDto> getSearchResultDtosFromKakaoSearch(KakaoMapSearchResponseDto kakaoMapSearchResponseDto) {

        if(kakaoMapSearchResponseDto == null || kakaoMapSearchResponseDto.getDocuments().isEmpty()) {
            return emptyList();
        }

        List<Document> documents = kakaoMapSearchResponseDto.getDocuments();
        return documents.stream()
                .map(document -> new SearchResultDto(document.getPlaceName(), document.getPlaceUrl()
                        , document.getCategoryName(), document.getAddressName(), document.getRoadAddressName()
                        , document.getCategoryGroupCode()
                        , document.getCategoryGroupName(), document.getX(), document.getY()))
                .toList();
    }

    private static List<SearchResultDto> getSearchResultDtosFromNaverSearch(NaverMapSearchResponseDto naverMapSearchResponseDto) {
        if(naverMapSearchResponseDto == null || naverMapSearchResponseDto.getItems().isEmpty()) {
            return emptyList();
        }
        List<Item> items = naverMapSearchResponseDto.getItems();
        return items.stream()
                .map(item -> new SearchResultDto(item.getTitle(), item.getLink(), item.getCategory(), item.getAddress(), item.getRoadAddress()
                        , item.getCategory(), item.getCategory(), item.getMapx(), item.getMapy()))
                .toList();
    }
    
    private void publishEventForSaveSearchKeyword(String keyword) {
        log.info("[SearchFacade] saveSearchResult - keyword: {}", keyword);
        eventPublisher.publishEvent(new SearchKeywordEventDto(keyword, LocalDateTime.now()));
    }

    private void publishEventForSaveSearchResult(SearchResponseDto response, String type) {
        log.info("[SearchFacade] publishSearchResult - response: {}", response);
        String key = getRedisKey(response.getKeyword(), type);
        int durationMinutes = getDurationMinutes(type);
        eventPublisher.publishEvent(new SearchResponseEventDto(key, response.getKeyword(), response.getResults(), durationMinutes, LocalDateTime.now()));
    }

    private String getRedisKey(String keyword, String type) {
        return switch (type) {
            case KAKAO -> "KAKAO:" + keyword;
            case NAVER -> "NAVER:" + keyword;
            case RESULT -> keyword;
            default -> keyword;
        };
    }
    private int getDurationMinutes( String type) {
        return switch (type) {
            case KAKAO -> 360;
            case NAVER -> 360;
            case RESULT -> 5;
            default -> 5;
        };
    }
    private String removeHtmlTagAndBlank(String str) {
        return str.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "").replaceAll(" ", "");
    }

}
