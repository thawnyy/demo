package com.kakaobank.practice.commons.lock;

import com.kakaobank.practice.commons.lock.ex.DistributionLockException;
import com.kakaobank.practice.commons.lock.support.DistributionLock;
import com.kakaobank.practice.commons.lock.util.SpELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Aspect
public class DistributionLockAspect {
    private final DistributionLocker distributionLocker;
    private final Random rand = new Random();

    @Pointcut("@annotation(com.kakaobank.practice.commons.lock.support.DistributionLock)")
    public void onInvoke() {
        // nothing
    }

    @Around("com.kakaobank.practice.commons.lock.DistributionLockAspect.onInvoke()")
    public Object tryLock(ProceedingJoinPoint pjp) throws Throwable {
        var signature = (MethodSignature) pjp.getSignature();
        var method = signature.getMethod();
        DistributionLock distributionLock = method.getAnnotation(DistributionLock.class);
        var keyPrefix = distributionLock.prefix();
        String distributionLockKey = keyPrefix + "-" + SpELParser.parameterToStringValue(
                signature.getParameterNames(), pjp.getArgs(), distributionLock.lockKey()
        );
        String lockUniqueKey = getLockUniqueKey(distributionLockKey);
        log.debug("DistributionLock key: {} | {} | run", distributionLockKey, lockUniqueKey);

        final boolean isAcquireLock = distributionLocker.tryLock(
                distributionLockKey,
                lockUniqueKey,
                distributionLock.waitTime(),
                distributionLock.leaseTime()
        );

        // lock 획득 실패에도 불구 하고 실행하도록 설정한 경우는 강제로 실행시킨다
        if (isAcquireLock) {
            log.info("DistributionLock key: {} | {} | acquire lock", distributionLockKey, lockUniqueKey);
            try {
                return pjp.proceed(pjp.getArgs());
            } finally {
                distributionLocker.unLock(lockUniqueKey);
                log.info("DistributionLock key: {} | {} | unlock", distributionLockKey, lockUniqueKey);
            }
        } else {
            log.info("DistributionLock key: {} | {} | acquire fail", distributionLockKey, lockUniqueKey);
            if (distributionLock.forceInvoke()) {
                return pjp.proceed(pjp.getArgs());
            }
            throw new DistributionLockException(method.getName() + " lock 획득 실패 종료");
        }
    }

    private String getLockUniqueKey(String lockKey) {
        return lockKey + (rand.nextInt(900) + 100);
    }
}
