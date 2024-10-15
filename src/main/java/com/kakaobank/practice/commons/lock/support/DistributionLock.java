package com.kakaobank.practice.commons.lock.support;

import com.kakaobank.practice.commons.lock.DistributionLocker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * lockKey: lock 용으로 레디스에 사용할 키
 * waitTime: lock 획득까지 대기 시간. 0일 경우 락을 획득하지 못할 경우 대기하지 않고 예외를 발생시킨다.
 * leaseTime: lock 유지 시간 (예외가 발생하여 unLock 호출을 못하는 경우에도 설정한 시간이 지나면 해제된다.)
 * forceInvoke: lock 획득하지 못했을 경우 실행여부
 *      false: 락을 획득하지 못할 경우 예외 발생
 *      true: 락을 획득하지 못하는 경우 경고와 함께 실행
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributionLock {
    // locKey 에 대한 prefix 를 선언 / 중복을 방지하기 위해
    String prefix() default "";
    // SpEL(Spring Expression Language (SpEL)) 을 사용한다.
    String lockKey();
    long waitTime() default DistributionLocker.NOT_WAIT;
    long leaseTime() default 1000;
    boolean forceInvoke() default false;
}
