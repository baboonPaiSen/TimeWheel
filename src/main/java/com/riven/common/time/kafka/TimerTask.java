package com.riven.common.time.kafka;


import lombok.Data;


/**
 * 任务基础类
 */
@Data
public abstract class TimerTask implements Runnable {

    /**
     * 表示当前任务延迟多久后执行(单位ms)，比如说延迟3s，则此值为3000
     */
    protected Long delayMs;


    /**
     * 指向TimerTaskEntry对象，一个TimerTaskEntry包含一个TimerTask，TimerTaskEntry是可复用的
     */
    private TimerTaskEntry timerTaskEntry;

    public TimerTaskEntry getTimerTaskEntry() {
        return timerTaskEntry;
    }

    /**
     * 设置当前任务绑定的TimerTaskEntry
     * @param entry
     */
    public void setTimerTaskEntry(TimerTaskEntry entry) {
        synchronized (this) {
            if (timerTaskEntry != null && timerTaskEntry != entry) {
                timerTaskEntry.remove();
            }
            timerTaskEntry = entry;
        }
    }


    /**
     * 取消当前任务，就是从TimerTaskEntry移出TimerTask，并且把当前的timerTaskEntry置空
     */
    public void cancel() {
        synchronized (this) {
            if (timerTaskEntry != null) {
                timerTaskEntry.remove();
            }
            timerTaskEntry = null;
        }
    }

}