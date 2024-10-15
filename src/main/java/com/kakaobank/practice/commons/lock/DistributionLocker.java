package com.kakaobank.practice.commons.lock;

import com.kakaobank.practice.commons.lock.ex.DistributionLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class DistributionLocker {
    public static final long NOT_WAIT = 0L;
    private final RedissonClient redissonClient;
    private final String namespace;
    private final Map<String, RLock> lockMap = new ConcurrentHashMap<>();

    /**
     * lock 획득을 시도한다.
     * @param key       : redis key
     * @param waitTime  : lock 획득까지 대기 시간
     * @param leaseTime : lock 유지 시간 (예외가 발생하여 unLock 호출을 못하는 경우에도 설정한 시간이 지나면 해제된다.)
     * @return 성공 시 true, 실패 시 false 를 반환한다.
     */
    public boolean tryLock(String key, String lockUniqueKey, long waitTime, long leaseTime) {
        return invoke(key, lockUniqueKey, waitTime, leaseTime);
    }

    private boolean invoke(String key, String lockUniqueKey, long waitTime, long leaseTime) throws DistributionLockException {
        var lockKey = getLockKey(key);
        var lock = redissonClient.getLock(lockKey);
        log.info("distribution lockKey: {}, lockUniqueKey: {}", lockKey, lockUniqueKey);
        try {
            var isAcquireLock = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            log.info("distribution isAcquireLock: {}", isAcquireLock);
            if (isAcquireLock) {
                log.info("lock 획득");
                lockMap.put(lockUniqueKey, lock);
                return true;
            } else {
                log.info("distribution isAcquireLock false waitTime : {}", waitTime);
                if (NOT_WAIT == waitTime) {
                    throw new DistributionLockException("활성화 Lock 존재");
                }
                return false;
            }
        } catch (Exception e) {
            log.warn("distribution lock fail: " + e.getMessage(), e);
            return false;
        }
    }

    public void unLock(String lockUniqueKey) {
        try {
            var currentLock = lockMap.get(lockUniqueKey);
            if (currentLock != null && currentLock.isLocked()) {
                currentLock.unlock();
            } else {
                log.debug("lock 이 이미 해제 되었습니다.");
            }
            lockMap.remove(lockUniqueKey);
        } catch (Exception e) {
            log.error("unLock fail: " + e.getMessage(), e);
        }
    }

    private String getLockKey(String key) {
        return namespace + key;
    }
}
