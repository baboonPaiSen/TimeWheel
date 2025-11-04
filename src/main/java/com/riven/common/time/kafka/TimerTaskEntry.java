package com.riven.common.time.kafka;

import lombok.Getter;
import lombok.Setter;


/**
 * 任务包装类TimerTaskEntry
 * TimerTaskEntry是TimerTask的包装类，实现了Compareable接口，用来比较两个任务的过期时间，以决定任务list插入的顺序
 */
@Getter
@Setter
public class TimerTaskEntry implements Comparable<TimerTaskEntry> {


    /**
     * 包含一个任务
     */
    protected TimerTask timerTask;

    /**
     *  任务的过期时间，此处的过期时间设置的过期间隔+系统当前时间(毫秒)
     */
    private Long expirationMs;


    /**
     * 当前任务属于哪一个列表
     */
    protected volatile TimerTaskList list ;


    /**
     * 当前任务的上一个任务，用双向列表连接
     */
    protected TimerTaskEntry next;
    protected TimerTaskEntry prev;


    public TimerTaskEntry(TimerTask timerTask, Long expirationMs) {
        this.timerTask = timerTask;
        this.expirationMs = expirationMs;
        //如果此timerTask已由现有的计时器任务条目保留
        // 则setTimerTaskEntry将删除它

        // 传递进来任务TimerTask，并设置TimerTask的包装类
        if (timerTask != null) {
            timerTask.setTimerTaskEntry(this);
        }
    }


    /**
     * 任务的取消，就是判断任务TimerTask的Entry是否是当前任务
     * @return
     */
    public Boolean cancelled() {
        return timerTask.getTimerTaskEntry() != this;
    }


    /**
     *  任务的移除
     */
    public void remove() {
        TimerTaskList currentList = this.list;
        // 如果在另一个线程将条目从一个任务条目列表移动到另一个任务条目列表时调用remove
        // 则由于list的值发生更改，该操作可能无法删除该条目。因此，重试，直到列表变为空
        // 在极少数情况下，此线程会看到null并退出循环，但另一个线程稍后会将条目插入另一个列表


        // 说明当前任务条目已经不再属于任何一个列表，循环终止
        while (currentList != null) {
            currentList.remove(this);
            currentList = list;
        }
    }


    /**
     *  比较两个任务在列表中的位置
     * @param that the object to be compared.
     * @return
     */
    @Override
    public int compareTo(TimerTaskEntry that) {
        return Long.compare(expirationMs, that.expirationMs);
    }


}