package com.kakaobank.practice;

import com.kakaobank.practice.commons.lock.support.DistributionLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
public class DemoTestConfiguration {
    @Bean
    public DistributionLockTestClass distributionLockTestClass() {
        return new DistributionLockTestClass();
    }
    @Slf4j
    public static class DistributionLockTestClass {
        @DistributionLock(
                prefix = "TEST_LOCK_WITH_EXCEPTION",
                lockKey = "#param1 + '-' + #param2",
                leaseTime = 3000
        )
        public void testLockWithException(String param1, String param2) throws InterruptedException {
            Thread.sleep(2000);
        }

        @DistributionLock(
                prefix = "TEST_LOCK_WITH_EXCEPTION",
                lockKey = "#param1 + '-' + #param2",
                leaseTime = 3000,
                forceInvoke = true
        )
        public void testLockForceInvokeWithException(String param1, String param2) throws InterruptedException {
            Thread.sleep(2000);
        }

        @DistributionLock(
                prefix = "TEST_LOCK",
                lockKey = "#param1 + '-' + #param2",
                waitTime = 5000,
                leaseTime = 5000
        )
        public void testLock(String param1, String param2) throws InterruptedException {
            Thread.sleep(2000);
        }
    }
}

