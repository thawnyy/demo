package com.kakaobank.practice.commons.lock.support;

import com.kakaobank.practice.commons.lock.DistributionLockConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 분산락을 활성화 시키기 위한 annotation
 * Configuration 이나 SpringApplication class 에 작성하면 기능이 활성화 된다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DistributionLockConfiguration.class)
public @interface EnableDistributionLock {
}
