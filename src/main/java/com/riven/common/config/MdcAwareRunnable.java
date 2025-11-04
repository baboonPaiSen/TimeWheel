package com.riven.common.config;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 包装Runnable以确保在线程开始时设置MDC，在结束时清理MDC
 */
public class MdcAwareRunnable implements Runnable {
    private final Runnable delegate;
    private final String traceId;

    public MdcAwareRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.traceId = getCurrentTraceId();
    }

    @Override
    public void run() {
        try {
            // 在任务执行之前设置MDC中的traceId
            MDC.put("traceId", traceId);
            delegate.run();
        } finally {
            // 确保在任务完成后清理MDC
            MDC.clear();
        }
    }


    private String getCurrentTraceId() {
        return UUID.randomUUID().toString().replace("-","");
    }

}
