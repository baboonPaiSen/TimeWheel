package com.riven.common.entity;

import com.riven.common.context.TimeWheelTaskManager;
import com.riven.common.cron.CronExpressionParser;
import com.riven.common.enums.TaskStatus;
import com.riven.common.job.SendMail;
import com.riven.common.mapper.SubscriptionInfoMapper;
import com.riven.common.time.kafka.TimerTask;
import com.riven.common.utils.SpringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
@Setter
public class WheelTask extends TimerTask {

    private Long taskId;

    private Long taskExecId;

    private Long userId;

    private String cron;

    private String clientId;

    private String taskType;

    private volatile TaskStatus taskStatus = TaskStatus.WAIT;

    private SendMail sendMailRunnable;


    public WheelTask(Long taskId, Long taskExecId, Long userId, String cron, String clientId, String taskType, SendMail sendMailRunnable) {
        this.taskId = taskId;
        this.taskExecId = taskExecId;
        this.userId = userId;
        this.cron = cron;
        this.clientId = clientId;
        this.taskType = taskType;
        this.sendMailRunnable = sendMailRunnable;
        this.delayMs = getDelay();
    }

    public WheelTask(Long taskId, Long taskExecId, Long userId, String cron, String clientId, String taskType, SendMail sendMailRunnable, Long delayMs) {
        this.taskId = taskId;
        this.taskExecId = taskExecId;
        this.userId = userId;
        this.cron = cron;
        this.clientId = clientId;
        this.taskType = taskType;
        this.sendMailRunnable = sendMailRunnable;
        this.delayMs = delayMs;
    }

    @Override
    public void run() {
        SubscriptionInfoMapper bean = SpringUtil.getBean(SubscriptionInfoMapper.class);
        try {
            List<SubscriptionInfo> subscriptionInfos = bean.selectAllById(taskId);
            if (StringUtils.isEmpty(subscriptionInfos) || !Objects.equals(subscriptionInfos.get(0).getClientId(),SpringUtil.getProfile())){
                //说明任务已经变动,无需执行
                TimeWheelTaskManager.getInstance().remove(this);
                return;
            }
            sendMailRunnable.run();
        } catch (Exception e) {
           log.error("执行邮件发送流程异常",e);
        }
        TimeWheelTaskManager.getInstance().remove(this);

        log.info("执行当前任务= taskId={}, taskExecId={}", this.taskId, this.taskExecId);
        if (!StringUtils.isEmpty(cron) && taskStatus != TaskStatus.CANCEL) {
            // 将下次执行计划添加至时间轮
            Long newId = this.taskExecId + 1;
            WheelTask wheelTask = new WheelTask(this.taskId, newId, this.userId, cron, SpringUtil.getProfile(), "mail", this.sendMailRunnable);
            TimeWheelTaskManager.getInstance().addTask(wheelTask);
        }

        SubscriptionInfo updateEntity = new SubscriptionInfo();
        updateEntity.setLastExecutionTime(new Date());
        updateEntity.setId(taskId);
        updateEntity.setNextExecutionTime(CronExpressionParser.getNextExecutionDate(this.cron));
        bean.updateByPrimaryKeySelective(updateEntity);

    }

    private long getDelay() {
        ZonedDateTime nextExecutionTime = CronExpressionParser.getNextExecutionZonedDateTime(cron);
        return nextExecutionTime.toInstant().toEpochMilli() - ZonedDateTime.now().toInstant().toEpochMilli();
    }


    @Override
    public String toString() {
        return "TaskBaseInfo{" +
                "taskId=" + taskId +
                ", taskExecId=" + taskExecId +
                ", userId=" + userId +
                ", cron='" + cron + '\'' +
                ", clientInstanceId='" + clientId + '\'' +
                ", taskType='" + taskType + '\'' +
                ", taskStatus=" + taskStatus +
                ", delayMs=" + delayMs +
                '}';
    }
}
