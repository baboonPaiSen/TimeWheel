package com.riven.common.entity;

import com.riven.common.context.DelayQueueTaskManager;
import com.riven.common.cron.CronExpressionParser;
import com.riven.common.enums.TaskStatus;
import com.riven.common.job.SendMail;
import com.riven.common.mapper.SubscriptionInfoMapper;
import com.riven.common.utils.SpringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class DelayedTask implements Runnable, Delayed {

    private Long taskId;

    private Long taskExecId;

    private Long userId;

    private String cron;

    private String clientInstanceId;

    private String taskType;

    private volatile TaskStatus taskStatus = TaskStatus.WAIT;

    private SendMail sendMailRunnable;

    private Long delayMs;

    /**
     *  任务的过期时间，此处的过期时间设置的过期间隔+系统当前时间(毫秒)
     */
    private Long expirationMs;


    public DelayedTask(Long taskId, Long taskExecId, Long userId, String cron, String clientInstanceId, String taskType, SendMail sendMailRunnable) {
        this.taskId = taskId;
        this.taskExecId = taskExecId;
        this.userId = userId;
        this.cron = cron;
        this.clientInstanceId = clientInstanceId;
        this.taskType = taskType;
        this.sendMailRunnable = sendMailRunnable;
        this.delayMs = getDelayByCron();
        this.expirationMs = System.currentTimeMillis() + delayMs;
    }

    public DelayedTask(Long taskId, Long taskExecId, Long userId, String cron, String clientInstanceId, String taskType, SendMail sendMailRunnable, Long delayMs) {
        this.taskId = taskId;
        this.taskExecId = taskExecId;
        this.userId = userId;
        this.cron = cron;
        this.clientInstanceId = clientInstanceId;
        this.taskType = taskType;
        this.sendMailRunnable = sendMailRunnable;
        this.delayMs = delayMs;
        this.expirationMs = System.currentTimeMillis() + delayMs;
    }

    @Override
    public void run() {
        SubscriptionInfoMapper bean = SpringUtil.getBean(SubscriptionInfoMapper.class);
        try {

            List<SubscriptionInfo> subscriptionInfos = bean.selectAllById(taskId);
            if (StringUtils.isEmpty(subscriptionInfos) || !Objects.equals(subscriptionInfos.get(0).getClientId(),SpringUtil.getProfile())){
                //说明任务已经变动,无需执行
                DelayQueueTaskManager.getInstance().remove(this);
                return;
            }
            sendMailRunnable.run();
        } catch (Exception e) {
           log.error("执行邮箱发送流程异常", e);
        }
        DelayQueueTaskManager.getInstance().remove(this);
        log.info("执行当前任务= taskId={}, taskExecId={}", this.taskId, this.taskExecId);
        if (!StringUtils.isEmpty(cron) && taskStatus != TaskStatus.CANCEL) {
            // 将下次执行计划添加至延时队列
            Long newId = this.taskExecId + 1;

            DelayedTask taskBaseInfo = new DelayedTask(this.taskId, newId, this.userId, cron, SpringUtil.getProfile(), "mail", this.sendMailRunnable,getDelayByCron());
            DelayQueueTaskManager.getInstance().addTask(taskBaseInfo);
        }

        SubscriptionInfo updateEntity = new SubscriptionInfo();
        updateEntity.setLastExecutionTime(new Date());
        updateEntity.setId(taskId);
        updateEntity.setNextExecutionTime(CronExpressionParser.getNextExecutionDate(this.cron));
        bean.updateByPrimaryKeySelective(updateEntity);

    }

    private long getDelayByCron() {
        ZonedDateTime nextExecutionTime = CronExpressionParser.getNextExecutionZonedDateTime(cron);
        return nextExecutionTime.toInstant().toEpochMilli() - ZonedDateTime.now().toInstant().toEpochMilli();
    }


    @Override
    public long getDelay(TimeUnit unit) {
        long diff = expirationMs - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }


    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }


    @Override
    public String toString() {
        return "DelayedTask{" +
                "taskId=" + taskId +
                ", taskExecId=" + taskExecId +
                ", userId=" + userId +
                ", cron='" + cron + '\'' +
                ", clientInstanceId='" + clientInstanceId + '\'' +
                ", taskType='" + taskType + '\'' +
                ", taskStatus=" + taskStatus +
                ", delayMs=" + delayMs +
                ", expirationMs=" + expirationMs +
                '}';
    }
}
