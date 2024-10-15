package com.kakaobank.practice.commons.lock.ex;

public class DistributionLockException extends RuntimeException {
    public DistributionLockException(String errorMessage) {
        super(errorMessage);
    }
}
