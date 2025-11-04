package com.riven.common.service;

import com.riven.common.config.ScheduleConfig;
import com.riven.common.context.DelayQueueTaskManager;
import com.riven.common.context.TimeWheelTaskManager;
import com.riven.common.entity.DelayedTask;
import com.riven.common.entity.SubscriptionInfo;
import com.riven.common.entity.WheelTask;
import com.riven.common.enums.ScheduleType;
import com.riven.common.utils.MailSendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GlobalTaskManagerService {

    @Autowired
    private ScheduleConfig scheduleConfig;


    public void addTask(SubscriptionInfo subscriptionInfo) {
        if (scheduleConfig.getType().equals(ScheduleType.TIME_WHEEL)) {

            TimeWheelTaskManager instance = TimeWheelTaskManager.getInstance();
            WheelTask wheelTask = new WheelTask(subscriptionInfo.getId(), 1L, subscriptionInfo.getUserId(), subscriptionInfo.getCronExpression(), subscriptionInfo.getClientId(), null, MailSendUtils.getSendMailRunnable(subscriptionInfo));
            instance.addTask(wheelTask);

        } else {
            DelayQueueTaskManager instance = DelayQueueTaskManager.getInstance();
            DelayedTask delayedTask = new DelayedTask(subscriptionInfo.getId(), 1L, subscriptionInfo.getUserId(), subscriptionInfo.getCronExpression(), subscriptionInfo.getClientId(), null, MailSendUtils.getSendMailRunnable(subscriptionInfo));
            instance.addTask(delayedTask);
        }
    }

    public void cancelTask(SubscriptionInfo subscriptionInfo) {
        if (scheduleConfig.getType().equals(ScheduleType.TIME_WHEEL)) {
            TimeWheelTaskManager instance = TimeWheelTaskManager.getInstance();
            instance.cancelByTaskId(subscriptionInfo.getId());
        } else {

            DelayQueueTaskManager instance = DelayQueueTaskManager.getInstance();
            instance.cancelByTaskId(subscriptionInfo.getId());

        }
    }


}
