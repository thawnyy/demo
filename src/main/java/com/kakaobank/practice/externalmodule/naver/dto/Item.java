package com.kakaobank.practice.externalmodule.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    /**
     * <title>조선옥</title>
     *             <link />
     *             <category>한식&gt;육류,고기요리</category>
     *             <description>연탄불 한우갈비 전문점.</description>
     *             <telephone></telephone>
     *             <address>서울특별시 중구 을지로3가 229-1 </address>
     *             <roadAddress>서울특별시 중구 을지로15길 6-5 </roadAddress>
     *             <mapx>311277</mapx>
     *             <mapy>552097</mapy>
     */
    private String title;
    private String link;
    private String category;
    private String description;
    private String telephone;
    private String address;
    private String roadAddress;
    private String mapx;
    private String mapy;

}
