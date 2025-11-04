package com.riven.common.time.kafka;

import com.riven.common.config.ThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author shiweinan
 */
@SuppressWarnings("AlibabaThreadPoolCreation")
@Slf4j
public class SystemTimer implements Timer {

    private Long tickMs = 1L;
    private Integer wheelSize = 20;
    private Long startMs = System.currentTimeMillis();
    private final TimingWheel timingWheel;
    private final DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();
    private final AtomicInteger taskCounter = new AtomicInteger(0);


    public SystemTimer() {

        timingWheel = new TimingWheel(tickMs, wheelSize, startMs, taskCounter, delayQueue);
    }

    public SystemTimer( Long tickMs, Integer wheelSize) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;

        timingWheel = new TimingWheel(tickMs, wheelSize, startMs, taskCounter, delayQueue);
    }

    public SystemTimer(Long tickMs, Integer wheelSize, Long startMs) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.startMs = startMs;
        timingWheel = new TimingWheel(tickMs, wheelSize, startMs, taskCounter, delayQueue);
        bossExecutor.submit((Runnable) () -> {
            log.info("bossExecutor主线程推动开始,时间间隔={}ms",1000);
            while (true) {
                advanceClock(1000L);
            }
        });
    }

    /**
     * 驱动线程池
     */
    private final ThreadPoolTaskExecutor taskExecutor = ThreadPool.getThreadPoolExecutor();

    // 创建一个自定义的ThreadFactory
    ThreadFactory namedThreadFactory = r -> new Thread(r, "TimeWheel-Thread");

    // 使用自定义的 ThreadFactory 创建单线程的线程池
    private final  ExecutorService bossExecutor = Executors.newSingleThreadExecutor(namedThreadFactory);

    /**
     * 用于在勾选时保护数据结构的锁
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();


    @Override
    public void add(TimerTask timerTask) {
        readLock.lock();
        try {
            log.info("添加时间轮任务, timeTask={}",timerTask);
            addTimerTaskEntry(new TimerTaskEntry(timerTask, timerTask.getDelayMs() + System.currentTimeMillis()));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 向Systemtimer 中添加一个任务，任务被包装为一个TimerTaskEntry
     *
     * @param timerTaskEntry timerTaskEntry
     */
    private void addTimerTaskEntry(TimerTaskEntry timerTaskEntry) {
        //先判断是否可以添加进时间轮中，如果不可以添加进去代表任务已经过期或者任务被取消
        // 注意这里的timingWheel持有上一层时间轮的引用，所以可能存在递归调用
        if (!timingWheel.add(timerTaskEntry)) {
            //取消
            if (!timerTaskEntry.cancelled()) {
                taskExecutor.submit(timerTaskEntry.timerTask);

            }
        }
    }

    @Override
    public Boolean advanceClock(Long timeoutMs) {

        log.info("执行推进,当前时间={}", new DateTime().toString("yyyy-MM-dd HH:mm:ss SSS"));
        try {
            TimerTaskList bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (bucket != null) {
                writeLock.lock();
                try {
                    while (bucket != null) {
                        // 驱动时间轮
                        timingWheel.advanceClock(bucket.getExpiration());
                        //循环bucket也就是任务列表，任务列表一个个继续添加进时间轮以此来升级或者降级时间轮，把过期任务找出来执行
                        bucket.flush(this::addTimerTaskEntry);
                        //这里就是从延迟队列取出bucket，bucket是有延迟时间的，取出代表该bucket过期，我们通过bucket能取到bucket包含的任务列表
                        bucket = delayQueue.poll();
                    }
                } finally {
                    writeLock.unlock();
                }
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error("执行异常", e);
            return Boolean.FALSE;
        }
    }

    @Override
    public Integer size() {
        return taskCounter.get();
    }

    @Override
    public void shutdown() {
        taskExecutor.shutdown();
        bossExecutor.shutdown();
    }
}