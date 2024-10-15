package com.kakaobank.practice.externalmodule.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverMapSearchResponseDto {
    private List<Item> items;

    // 생성자
    public NaverMapSearchResponseDto(List<Item> items) {
        this.items = items;
    }

    public static NaverMapSearchResponseDto mock() {
        return new NaverMapSearchResponseDto(
                List.of(
                        new Item("A", "a", "a", "a", "a", "a", "a", "a", "a"),
                        new Item("B", "b", "b", "b", "b", "b", "b", "b", "b"),
                        new Item("S 광적점", "c", "c", "c", "c", "c", "c", "c", "c"),
                        new Item("다ddd", "d", "d", "d", "d", "d", "d", "d", "d"),
                        new Item("C", "e", "e", "e", "e", "e", "e", "e", "e")
                )
        );
    }

}

