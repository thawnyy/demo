package com.kakaobank.practice.commons.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * 분산락 기능에 필요한 bean 을 로딩한다.
 */
@Slf4j
public class DistributionLockConfiguration {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.application.name}")
    private String applicationName;

    private static final String REDISSON_HOST_PREFIX = "redis://";

    @Bean
    public DistributionLocker distributionLocker() {
        var namespace = applicationName + ":";
        return new DistributionLocker(redissonClient(), namespace);
    }

    @Bean
    public DistributionLockAspect distributionLockAspect(DistributionLocker distributionLocker) {
        return new DistributionLockAspect(distributionLocker);
    }
    private RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }
}
