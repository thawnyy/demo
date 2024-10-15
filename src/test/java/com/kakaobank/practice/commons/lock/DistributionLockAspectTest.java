package com.kakaobank.practice.commons.lock;

import com.kakaobank.practice.DemoTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import redis.embedded.RedisServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = {DemoTestConfiguration.class, DistributionLockConfiguration.class, AopAutoConfiguration.class})
public class DistributionLockAspectTest {
    private static RedisServer redisServer = new RedisServer(6380);
    @Autowired
    private DemoTestConfiguration.DistributionLockTestClass distributionLockTestClass;

    @BeforeAll
    public static void redisStart() {
        try {
            redisServer.start();
        } catch (Exception e) {
            log.info("redis server start fail");
            redisServer.stop();
        }
    }

    @AfterAll
    public static void redisStop() {
        try {
            redisServer.stop();
        } catch (Exception e) {
            log.info("redis server stop fail");
        }
    }

    @Test
    public void lock_획득_실패시_오류_발생테스트() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Collection<Callable<Void>> tasks = new ArrayList<>();

        AtomicInteger successCount = new AtomicInteger();
        for (int i = 0 ; i < 5 ; i++) {
            tasks.add(() -> {
                distributionLockTestClass.testLockWithException("key1", "key2");
                successCount.incrementAndGet();
                return null;
            });
        }

        executorService.invokeAll(tasks);

        assertThat(1)
                .as("lock 제한으로 인해 1회만 성공")
                .isEqualTo(successCount.get());
    }

    @Test
    public void lock_획득_실패_강제실행_테스트() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Collection<Callable<Void>> tasks = new ArrayList<>();

        AtomicInteger successCount = new AtomicInteger();
        for (int i = 0 ; i < 5 ; i++) {
            tasks.add(() -> {
                distributionLockTestClass.testLockForceInvokeWithException("key1", "key2");
                successCount.incrementAndGet();
                return null;
            });
        }

        executorService.invokeAll(tasks);

        assertThat(5)
                .as("lock 실행 실패에도 강제실행(forceInvoke)으로 인해 5회 성공")
                .isEqualTo(successCount.get());
    }

    @Test
    public void lock_획득을_위해_대기하고_실행테스트() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Collection<Callable<Void>> tasks = new ArrayList<>();

        AtomicInteger successCount = new AtomicInteger();
        for (int i = 0 ; i < 3 ; i++) {
            tasks.add(() -> {
                distributionLockTestClass.testLock("key1", "key2");
                successCount.incrementAndGet();
                return null;
            });
        }

        executorService.invokeAll(tasks);

        assertThat(3)
                .as("lock 획득을 위해 대기 설정: 5초 | " +
                        "마지막 스레드의 경우 앞의 두개 스레드가 종료될 때까지 기다려야 하므로 4초까지 대기가 발생한다. " +
                        "약간의 지연이 있을 수 있으므로 대기시간은 5초로 설정")
                .isEqualTo(successCount.get());
    }
}