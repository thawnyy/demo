package com.kakaobank.practice.searchkeyword.adapter;

import com.kakaobank.practice.searchkeyword.domain.SearchKeyword;
import com.kakaobank.practice.searchkeyword.domain.SearchKeywordHistory;
import com.kakaobank.practice.searchkeyword.dto.SearchKeywordEventDto;
import com.kakaobank.practice.searchkeyword.repository.SearchKeywordHistoryRepository;
import com.kakaobank.practice.searchkeyword.repository.SearchKeywordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SearchKeywordEventHandlerTest {

    @Mock
    private SearchKeywordRepository searchKeywordRepository;
    @Mock
    private SearchKeywordHistoryRepository searchKeywordHistoryRepository;

    @InjectMocks
    private SearchKeywordEventHandler searchKeywordEventHandler;

    private final String keyword = "테스트_검색어";

    @Test
    void 기존재_키워드_카운팅() {
        // Given
        SearchKeywordEventDto event = new SearchKeywordEventDto(keyword, LocalDateTime.now());

        SearchKeyword existingKeyword = SearchKeyword.of(keyword, 5L);
        when(searchKeywordRepository.findByKeyword(keyword)).thenReturn(Optional.of(existingKeyword));
        when(searchKeywordHistoryRepository.countByKeyword(keyword)).thenReturn(10L);

        // When
        searchKeywordEventHandler.handle(event);

        // Then
        assert(existingKeyword.getCount() == 10);
    }

    @Test
    void 신규키워드_저장() {
        // Given
        String keyword = "newKeyword";
        SearchKeywordEventDto event = new SearchKeywordEventDto(keyword, LocalDateTime.now());

        when(searchKeywordRepository.findByKeyword(keyword)).thenReturn(Optional.empty());
        when(searchKeywordHistoryRepository.countByKeyword(keyword)).thenReturn(5L);

        // When
        searchKeywordEventHandler.handle(event);

        // Then
        verify(searchKeywordHistoryRepository).save(any(SearchKeywordHistory.class));
        verify(searchKeywordRepository).save(any(SearchKeyword.class));
        verify(searchKeywordRepository).findByKeyword(keyword);
    }

    @Test
    void 동시성_테스트() throws InterruptedException {
        // Given
        int threadCount = 100;
        SearchKeywordEventDto event = new SearchKeywordEventDto(keyword, LocalDateTime.now());

        when(searchKeywordRepository.findByKeyword(keyword)).thenReturn(Optional.of(SearchKeyword.of(keyword, 0L)));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> searchKeywordEventHandler.handle(event));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Then
        verify(searchKeywordHistoryRepository, times(threadCount)).save(any(SearchKeywordHistory.class));
        verify(searchKeywordRepository, times(threadCount)).findByKeyword(keyword);
        verify(searchKeywordRepository, times(threadCount)).save(any(SearchKeyword.class));

    }
}