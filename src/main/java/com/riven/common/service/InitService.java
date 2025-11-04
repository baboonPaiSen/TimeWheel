package com.riven.common.service;


import com.riven.common.config.ScheduleConfig;
import com.riven.common.context.DelayQueueTaskManager;
import com.riven.common.context.TimeWheelTaskManager;
import com.riven.common.cron.CronExpressionParser;
import com.riven.common.entity.DelayedTask;
import com.riven.common.entity.SubscriptionInfo;
import com.riven.common.entity.WheelTask;
import com.riven.common.enums.ScheduleType;
import com.riven.common.enums.SubscriptionType;
import com.riven.common.job.SendMail;
import com.riven.common.mapper.SubscriptionInfoMapper;
import com.riven.common.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static com.riven.common.utils.MailSendUtils.getSendMailRunnable;

@Service
@Slf4j
@DependsOn({"springUtil"})
public class InitService {




    @Autowired
    private SubscriptionInfoMapper subscriptionInfoMapper;

    @Autowired
    private ScheduleConfig scheduleConfig;

    @PostConstruct
    public void init() {


        if (scheduleConfig.getType().equals(ScheduleType.TIME_WHEEL)) {
            TimeWheelTaskManager instance = TimeWheelTaskManager.getInstance();
            List<SubscriptionInfo> subscriptionInfos = subscriptionInfoMapper.selectAllByClientIdAndSubscriptionType(SpringUtil.getProfile(), SubscriptionType.EMAIL.getCode());
            if (!CollectionUtils.isEmpty(subscriptionInfos)) {
                for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {

                    SendMail sendMail = getSendMailRunnable(subscriptionInfo);

                    List<ZonedDateTime> cronExecutionTimes = CronExpressionParser.getCronExecutionTimes(subscriptionInfo.getCronExpression(), subscriptionInfo.getLastExecutionTime(), new Date());
                    WheelTask wheelTask = null;
                    if (!CollectionUtils.isEmpty(cronExecutionTimes)) {
                        log.info("发现未及时执行的任务,taskId+{}", subscriptionInfo.getId());
                        wheelTask = new WheelTask(subscriptionInfo.getId(), 1L, subscriptionInfo.getUserId(), subscriptionInfo.getCronExpression(), subscriptionInfo.getClientId(), null, sendMail, 1000L);
                    } else {
                        wheelTask = new WheelTask(subscriptionInfo.getId(), 1L, subscriptionInfo.getUserId(), subscriptionInfo.getCronExpression(), subscriptionInfo.getClientId(), null, sendMail);
                    }

                    instance.addTask(wheelTask);
                }
            }
        } else {
            DelayQueueTaskManager instance = DelayQueueTaskManager.getInstance();
            List<SubscriptionInfo> subscriptionInfos = subscriptionInfoMapper.selectAllByClientIdAndSubscriptionType(SpringUtil.getProfile(), SubscriptionType.EMAIL.getCode());
            if (!CollectionUtils.isEmpty(subscriptionInfos)) {
                for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {
                    SendMail sendMail = getSendMailRunnable(subscriptionInfo);

                    List<ZonedDateTime> cronExecutionTimes = CronExpressionParser.getCronExecutionTimes(subscriptionInfo.getCronExpression(), subscriptionInfo.getLastExecutionTime(), new Date());
                    DelayedTask delayedTask = null;
                    if (!CollectionUtils.isEmpty(cronExecutionTimes)) {
                        log.info("发现未及时执行的任务,taskId+{}", subscriptionInfo.getId());
                        delayedTask = new DelayedTask(subscriptionInfo.getId(), 1L, subscriptionInfo.getUserId(), subscriptionInfo.getCronExpression(), subscriptionInfo.getClientId(), null,
                                sendMail, 1000L);
                    } else {
                        delayedTask = new DelayedTask(subscriptionInfo.getId(), 1L, subscriptionInfo.getUserId(), subscriptionInfo.getCronExpression(), subscriptionInfo.getClientId(), null, sendMail);
                    }

                    instance.addTask(delayedTask);
                }

            }


        }
    }


}
