package com.riven.common.service;

import com.riven.common.config.RetryPolicy;
import com.riven.common.config.ThreadPool;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RetryService {

    private final RetryPolicy retryPolicy;

    private final Long taskId;

    public RetryService(RetryPolicy retryPolicy,Long taskId) {
        this.taskId = taskId;
        this.retryPolicy = retryPolicy;
    }

    public void executeWithRetry(RetryCallback callback) {
        executeWithRetry(callback, 1);
    }

    private void executeWithRetry(RetryCallback callback, int attempt) {
        if (attempt >= retryPolicy.getMaxRetries()) {
            log.error("达到最大尝试次数: {}", attempt);
            return;
        }
        try {
            callback.call();
            log.info("taskId={}====>重试任务执行成功",taskId);
        } catch (Exception e) {
            log.error("taskId={}====>任务执行失败,准备尝试：第 {} 次", taskId, attempt, e);
            long delay = (long) (retryPolicy.getInitialDelay() * Math.pow(retryPolicy.getBackoffFactor(), attempt));
            ThreadPool.getScheduledThreadPoolExecutor().schedule(() -> executeWithRetry(callback, attempt + 1), delay, TimeUnit.MILLISECONDS);
        }
    }

}