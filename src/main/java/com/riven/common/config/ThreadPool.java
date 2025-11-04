package com.riven.common.config;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 线程池配置
 **/
public class ThreadPool {

    // 核心线程池大小
    private static final int CORE_POOL_SIZE = 15;

    // 最大可创建的线程数
    private static final int MAX_POOL_SIZE = 100;

    // 队列最大长度
    private static final int QUEUE_CAPACITY = 1000;

    // 线程池维护线程所允许的空闲时间
    private static final int KEEP_ALIVE_SECONDS = 300;

    // 定时线程池核心线程数
    private static final int SCHEDULED_CORE_POOL_SIZE = 5;

    // 线程池实例
    private static  ThreadPoolTaskExecutor threadPoolExecutor;

    // 定时线程池实例
    private static  ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    // 普通线程池初始化标志
    private static boolean threadPoolInitialized = false;

    // 定时线程池初始化标志
    private static boolean scheduledThreadPoolInitialized = false;


    /**
     * 创建带有MDC支持的自定义ThreadFactory
     */
    private static ThreadFactory createMdcAwareThreadFactory(final String threadNamePrefix) {
        return r -> {
            Thread t = new Thread(new MdcAwareRunnable(r), threadNamePrefix + "-" + Thread.activeCount());
            t.setDaemon(false);
            return t;
        };
    }



    /**
     * 创建并初始化带有MDC支持的线程池
     */
    private static ThreadPoolTaskExecutor createMdcAwareThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix("executor-pool-");
        executor.setThreadFactory(createMdcAwareThreadFactory(executor.getThreadNamePrefix()));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.afterPropertiesSet(); // Initialize the executor
        return executor;
    }



    /**
     * 创建并初始化带有MDC支持的定时线程池
     */
    private static ScheduledThreadPoolExecutor createMdcAwareScheduledThreadPoolExecutor() {
        return new ScheduledThreadPoolExecutor(SCHEDULED_CORE_POOL_SIZE,
                createMdcAwareThreadFactory("schedule-pool-")) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
            }
        };
    }


    // 修改获取线程池实例的方法，使用新的创建方法
    public static ThreadPoolTaskExecutor getThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            synchronized (ThreadPool.class) {
                if (threadPoolExecutor == null && !threadPoolInitialized) {
                    threadPoolExecutor = createMdcAwareThreadPoolExecutor();
                    threadPoolInitialized = true;
                }
            }
        }
        return threadPoolExecutor;
    }

    // 修改获取定时线程池实例的方法，使用新的创建方法
    public static ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        if (scheduledThreadPoolExecutor == null) {
            synchronized (ThreadPool.class) {
                if (scheduledThreadPoolExecutor == null && !scheduledThreadPoolInitialized) {
                    scheduledThreadPoolExecutor = createMdcAwareScheduledThreadPoolExecutor();
                    scheduledThreadPoolInitialized = true;
                }
            }
        }
        return scheduledThreadPoolExecutor;
    }

}