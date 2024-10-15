package com.kakaobank.practice;

import com.kakaobank.practice.externalmodule.kakao.KakaoSearchProperties;
import com.kakaobank.practice.externalmodule.naver.NaverSearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@EnableJpaAuditing
@Configuration
@EnableConfigurationProperties({
        KakaoSearchProperties.class,
        NaverSearchProperties.class
})
public class DemoConfiguration {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        // RestTemplate 설정 (3초 연결, 읽기 타임아웃)
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate template = new RetryTemplate();

        // Retry policy 설정 (최대 3회 재시도)
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        template.setRetryPolicy(retryPolicy);

        // Backoff policy 설정 (재시도 사이에 100ms 대기)
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(100);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }
}
