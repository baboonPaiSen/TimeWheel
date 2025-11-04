package com.riven.common.context;

import com.riven.common.entity.WheelTask;
import com.riven.common.enums.TaskStatus;
import com.riven.common.time.kafka.SystemTimer;
import com.riven.common.time.kafka.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class TimeWheelTaskManager {

    private  static TimeWheelTaskManager instance;

    private volatile ConcurrentHashMap<Long, WheelTask> cache = new ConcurrentHashMap<>();

    private final SystemTimer timer;


    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();


    private TimeWheelTaskManager() {
        // 私有构造函数，防止外部实例化
        timer = new SystemTimer(1000L, 60, System.currentTimeMillis());
    }

    public void cancelByTaskId(Long id) {
        writeLock.lock();
        try {
            // 取消可能还未执行的任务
            log.info("取消任务开始, taskId={}", id);
            WheelTask find = cache.get(id);
            if (find != null) {
                log.info("发现当前待执行的任务, taskId={},taskExecId={}", find.getTaskId(),find.getTaskExecId());
                find.setTaskStatus(TaskStatus.CANCEL);
                find.cancel();
                remove(find); // 直接调用 removeCache 而不是再次加锁
            }
            log.info("取消任务未发现该任务,放弃,taskId={}",id);
        } finally {
            writeLock.unlock();
        }
    }

    public void remove(TimerTask timerTask) {
        writeLock.lock();
        try {
            if (timerTask instanceof WheelTask) {
                WheelTask wheelTask = (WheelTask) timerTask;
                WheelTask removed = cache.remove(wheelTask.getTaskId());
                if (removed != null) {
                    log.info("清除缓存成功, taskId={},taskExecId={}", wheelTask.getTaskId(), wheelTask.getTaskExecId());
                } else {
                    log.error("清除缓存失败, 可能是已经执行完毕taskId={},taskExecId={}", wheelTask.getTaskId(), wheelTask.getTaskExecId());
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public WheelTask getTaskById(Long taskId) {
        readLock.lock();
        try {
            return cache.get(taskId);
        } finally {
            readLock.unlock();
        }
    }


    public List<WheelTask> getAllTask() {
        return new ArrayList<>(cache.values());

    }

    public void addTask(WheelTask wheelTask) {

        ConcurrentHashMap<Long, WheelTask> temp = cache;
        writeLock.lock();
        try {
            while (temp.containsKey(wheelTask.getTaskId())) {
                temp = cache;
                log.info("添加任务发现任务未执行完毕,等待====> taskId={},taskExecId={}", wheelTask.getTaskId(), wheelTask.getTaskExecId());
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("等待异常",e);
                }
            }
            cache.put(wheelTask.getTaskId(), wheelTask);
            timer.add(wheelTask);
        }catch (Exception e)
        {
            log.error("添加任务异常",e);
        }finally {
            writeLock.unlock();
        }
    }

    // 获取单例实例的方法
    public static TimeWheelTaskManager getInstance() {
        if (instance == null) {
            synchronized (TimeWheelTaskManager.class) {
                if (instance == null) {
                    instance = new TimeWheelTaskManager();
                }
            }
        }
        return instance;
    }
}
