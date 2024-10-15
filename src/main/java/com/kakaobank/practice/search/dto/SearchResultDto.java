package com.kakaobank.practice.search.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public class SearchResultDto {
    /**
     *
     *       "place_name": "카카오프렌즈 코엑스점",
     *       "distance": "418",
     *       "place_url": "http://place.map.kakao.com/26338954",
     *       "category_name": "가정,생활 > 문구,사무용품 > 디자인문구 > 카카오프렌즈",
     *       "address_name": "서울 강남구 삼성동 159",
     *       "road_address_name": "서울 강남구 영동대로 513",
     *       "id": "26338954",
     *       "phone": "02-6002-1880",
     *       "category_group_code": "",
     *       "category_group_name": "",
     *       "x": "127.05902969025047",
     *       "y": "37.51207412593136"
     */
    private final String placeName;
    private final String placeUrl;
    private final String categoryName;
    private final String addressName;
    private final String roadAddressName;
    private final String categoryGroupCode;
    private final String categoryGroupName;
    private final String x;
    private final String y;

    public SearchResultDto(String placeName, String placeUrl, String categoryName, String addressName, String roadAddressName, String categoryGroupCode, String categoryGroupName, String x, String y) {
        this.placeName = placeName;
        this.placeUrl = placeUrl;
        this.categoryName = categoryName;
        this.addressName = addressName;
        this.roadAddressName = roadAddressName;
        this.categoryGroupCode = categoryGroupCode;
        this.categoryGroupName = categoryGroupName;
        this.x = x;
        this.y = y;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SearchResultDto other)) return false;
        return removeHtmlTagAndBlank(placeName).equals(removeHtmlTagAndBlank(other.placeName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(removeHtmlTagAndBlank(placeName));
    }

    private String removeHtmlTagAndBlank(String str) {
        return str.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "").replaceAll(" ", "");
    }
}
