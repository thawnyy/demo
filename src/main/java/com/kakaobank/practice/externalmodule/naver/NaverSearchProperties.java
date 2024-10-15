package com.kakaobank.practice.externalmodule.naver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "naver-search")
public class NaverSearchProperties {
    private Api api;

    @Setter
    @Getter
    public static class Api {
        private String host;
        private String clientId;
        private String clientSecret;
    }
}
