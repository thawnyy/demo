package com.kakaobank.practice.search.facade;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaobank.practice.externalmodule.kakao.dto.Document;
import com.kakaobank.practice.externalmodule.kakao.dto.KakaoMapSearchResponseDto;
import com.kakaobank.practice.externalmodule.kakao.service.KakaoMapSearchService;
import com.kakaobank.practice.externalmodule.naver.dto.Item;
import com.kakaobank.practice.externalmodule.naver.dto.NaverMapSearchResponseDto;
import com.kakaobank.practice.externalmodule.naver.service.NaverMapSearchService;
import com.kakaobank.practice.search.dto.SearchResponseDto;
import com.kakaobank.practice.search.dto.SearchResponseEventDto;
import com.kakaobank.practice.search.dto.SearchResultDto;
import com.kakaobank.practice.searchkeyword.dto.SearchKeywordEventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SearchFacadeTest {

    @Mock
    private KakaoMapSearchService kakaoMapSearchService;

    @Mock
    private NaverMapSearchService naverMapSearchService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations; // ValueOperations Mock 추가

    @InjectMocks
    private SearchFacade searchFacade;

    private final String keyword = "testKeyword";
    private final String kakaoKey = "KAKAO:" + keyword;
    private final String naverKey = "NAVER:" + keyword;
    private final String resultKey = keyword;

    @BeforeEach
    void setUp() {
        // 설정된 Mock 객체의 기본 동작
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 검색어_발행이벤트_테스트() {
        // When
        searchFacade.search(keyword);

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(SearchKeywordEventDto.class));
    }

    @Test
    void 카카오_검색성공_네이버_검색성공_테스트_검색결과_중복존재() throws IOException {
        // When
        when(kakaoMapSearchService.search(keyword)).thenReturn(KakaoMapSearchResponseDto.mock());
        when(naverMapSearchService.search(keyword)).thenReturn(NaverMapSearchResponseDto.mock());

        SearchResponseDto response = searchFacade.search(keyword);

        // Then
        assertNotNull(response);
        assertEquals(7, response.getResults().size()); // 카카오와 네이버 결과 포함

        verify(eventPublisher, times(3)).publishEvent(any(SearchResponseEventDto.class)); // 검색 결과 이벤트 발행 확인
    }


    @Test
    void 모든_검색엔진_성공_중복X() {
        // Given
        KakaoMapSearchResponseDto kakaoMapSearchResponseDto = new KakaoMapSearchResponseDto(List.of(new Document("Place1", "distinct", "url1", "category1", "address1", "roadAddress1", "groupCode1", "groupName1", "1.0", "1.0", "1.0", "1.0")));
        NaverMapSearchResponseDto naverMapSearchResponseDto = new NaverMapSearchResponseDto(List.of(new Item("Place2", "url2", "category2", "address2", "roadAddress2", "groupCode2", "groupName2", "1.0", "1.0")));

        // When
        when(kakaoMapSearchService.search(keyword)).thenReturn(kakaoMapSearchResponseDto);
        when(naverMapSearchService.search(keyword)).thenReturn(naverMapSearchResponseDto);
        SearchResponseDto response = searchFacade.search(keyword);

        // Then
        verify(kakaoMapSearchService).search(keyword);
        verify(naverMapSearchService).search(keyword);
        assertEquals(2, response.getResults().size()); // 중복 없이 결과를 조합해야 하므로
    }

    @Test
    void 레디스_검색결과를_이용하여_응답() throws IOException {
        // Given
        SearchResponseDto redisResponse = new SearchResponseDto(keyword, Collections.emptyList());

        when(valueOperations.get(resultKey)).thenReturn("redisResponse");
        when(objectMapper.readValue("redisResponse", SearchResponseDto.class)).thenReturn(redisResponse);

        // When
        SearchResponseDto response = searchFacade.search(keyword);

        // Then
        verify(valueOperations).get(resultKey);
        verify(kakaoMapSearchService, never()).search(any());
        verify(naverMapSearchService, never()).search(any());
        verify(eventPublisher).publishEvent(any(SearchKeywordEventDto.class));
        verify(eventPublisher, never()).publishEvent(any(SearchResponseEventDto.class));
        assertEquals(redisResponse, response);
    }

    @Test
    void 카카오_검색성공_네이버_검색실패_케이스() throws IOException {
        // Given
        when(valueOperations.get(keyword)).thenReturn(null);
        when(kakaoMapSearchService.search(keyword)).thenReturn(KakaoMapSearchResponseDto.mock());
        when(naverMapSearchService.search(keyword)).thenThrow(new RuntimeException("Naver search failed"));
        SearchResponseDto naverRedisResponse = new SearchResponseDto(keyword, Collections.singletonList(new SearchResultDto("Naver Place", "http://naver.com", "Category", "Address", "Road Address", "Category Group Code", "Category Group Name", "1.0", "1.0")));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(naverKey)).thenReturn("naverRedisResponse");
        when(objectMapper.readValue("naverRedisResponse", SearchResponseDto.class)).thenReturn(naverRedisResponse);

        // When
        SearchResponseDto response = searchFacade.search(keyword);

        // Then
        assertNotNull(response);
        assertEquals(6, response.getResults().size()); // 카카오 결과 + Redis에서 가져온 네이버 결과

        // Verify interactions
        verify(kakaoMapSearchService).search(keyword); // Kakao search called
        verify(naverMapSearchService).search(keyword); // Naver search called
        verify(valueOperations).get(naverKey); // Redis for Naver search called
    }

    @Test
    void 네이버_검색성공_카카오_검색실패_케이스() throws IOException {

        // Given
        when(valueOperations.get(keyword)).thenReturn(null);
        when(naverMapSearchService.search(keyword)).thenReturn(NaverMapSearchResponseDto.mock());
        when(kakaoMapSearchService.search(keyword)).thenThrow(new RuntimeException("Kakao error"));

        SearchResponseDto kakaoRedisResponse = new SearchResponseDto(keyword, Collections.singletonList(new SearchResultDto("Naver Place", "http://naver.com", "Category", "Address", "Road Address", "Category Group Code", "Category Group Name", "1.0", "1.0")));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(kakaoKey)).thenReturn("kakaoRedisResponse");
        when(objectMapper.readValue("kakaoRedisResponse", SearchResponseDto.class)).thenReturn(kakaoRedisResponse);

        // When
        SearchResponseDto response = searchFacade.search(keyword);

        // Then
        verify(naverMapSearchService).search(keyword);
        verify(kakaoMapSearchService).search(keyword);
        assertEquals(response.getResults().size(), 6);
    }

}

