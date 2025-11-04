package com.riven.common.config;

public class RetryPolicy {
    private final int maxRetries;
    private final long initialDelay;
    private final double backoffFactor;

    public RetryPolicy(int maxRetries, long initialDelay, double backoffFactor) {
        this.maxRetries = maxRetries;
        this.initialDelay = initialDelay;
        this.backoffFactor = backoffFactor;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public double getBackoffFactor() {
        return backoffFactor;
    }
}
