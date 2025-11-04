package com.riven.common.service;

@FunctionalInterface
public interface RetryCallback {
    void call() ;
}
