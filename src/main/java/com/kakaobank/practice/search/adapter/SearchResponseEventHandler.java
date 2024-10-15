package com.kakaobank.practice.search.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaobank.practice.search.dto.SearchResponseDto;
import com.kakaobank.practice.search.dto.SearchResponseEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchResponseEventHandler {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @EventListener
    public void handle(SearchResponseEventDto event) {
        log.info("[SearchResponseEventHandler] handle - event: {}", event);

        // 검색 결과를 redis에 저장 - 5분간 유효
        try {
            String value = objectMapper.writeValueAsString(new SearchResponseDto(event.keyword(), event.results()));
            log.info("[SearchFacade] save SearchResult Redis - value: {} durationMinutes: {}", value, event.durationMinutes());
            redisTemplate.opsForValue().set(event.key(), value, event.durationMinutes(), TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("[SearchFacade] search - error: {}", e.getMessage());
        }
    }
}
