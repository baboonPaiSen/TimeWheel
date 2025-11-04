package com.riven.common.context;

import com.riven.common.config.ThreadPool;
import com.riven.common.enums.TaskStatus;
import com.riven.common.entity.DelayedTask;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class DelayQueueTaskManager {

    private static DelayQueueTaskManager instance;

    private final DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();
    private final ScheduledExecutorService scheduler =ThreadPool.getScheduledThreadPoolExecutor();

    private volatile ConcurrentHashMap<Long, DelayedTask> cache = new ConcurrentHashMap<>();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private DelayQueueTaskManager() {
        // 启动消费者线程
        scheduler.scheduleAtFixedRate(() -> {

            log.info("执行推进,当前时间={}", new DateTime().toString("yyyy-MM-dd HH:mm:ss SSS"));
            try {
                // 尝试从队列中拉取一个任务
                DelayedTask task = delayQueue.poll();
                if (task != null && task.getTaskStatus() != TaskStatus.CANCEL) {
                    ThreadPool.getThreadPoolExecutor().submit(task);
                } else if (task != null) {
                    log.info("任务已取消 不再执行: taskId={}, taskExecId={}", task.getTaskId(), task.getTaskExecId());
                }
            } catch (Exception e) {
                log.error("消费者线程错误", e);
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    public void cancelByTaskId(Long taskId) {
        writeLock.lock();
        try {
            log.info("取消任务开始 taskId={}", taskId);
            DelayedTask task = cache.get(taskId);
            if (task != null) {
                task.setTaskStatus(TaskStatus.CANCEL);
                log.info("任务已标记为取消, taskId={}, taskExecId={}", task.getTaskId(), task.getTaskExecId());
            }

            log.info("取消任务未发现该任务,放弃,taskId={}",taskId);
        } finally {
            writeLock.unlock();
        }
    }

    public void remove(DelayedTask task) {
        writeLock.lock();
        try {
            DelayedTask removed = cache.remove(task.getTaskId());
            if (removed != null) {
//                log.info("清除缓存成功, taskId={}, taskExecId={}", task.getTaskId(), task.getTaskExecId());
            } else {
                log.error("清除缓存失败, 可能是已经执行完毕或不存在 taskId={}, taskExecId={}", task.getTaskId(), task.getTaskExecId());
            }
        } finally {
            writeLock.unlock();
        }
    }

    public DelayedTask getTaskById(Long taskId) {
        readLock.lock();
        try {
            return cache.get(taskId);
        } finally {
            readLock.unlock();
        }
    }

    public List<DelayedTask> getAllTask() {
        return new ArrayList<>(cache.values());

    }


    public void addTask(DelayedTask task) {
        writeLock.lock();
        try {
            if (cache.containsKey(task.getTaskId())) {
                log.warn("任务已存在 不再重复添加: task={}", task);
                return;
            }
            cache.put(task.getTaskId(), task);
            delayQueue.put(task);
            log.info("任务添加成功, task={}", task);
        } finally {
            writeLock.unlock();
        }
    }

    public static DelayQueueTaskManager getInstance() {
        if (instance == null) {
            synchronized (DelayQueueTaskManager.class) {
                if (instance == null) {
                    instance = new DelayQueueTaskManager();
                }
            }
        }
        return instance;
    }
}
