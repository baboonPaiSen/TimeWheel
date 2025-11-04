package com.riven.common.utils;

import com.riven.common.entity.MailSendEntity;
import com.riven.common.entity.SubscriptionInfo;
import com.riven.common.job.SendMail;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MailSendUtils {

    public static void sendMail(MailSendEntity mailSendEntity) {
        // 这里应该实现实际的邮件发送逻辑
        log.info("发送邮�? {}", mailSendEntity);
    }

    public static SendMail getSendMailRunnable(SubscriptionInfo subscriptionInfo) {
        return new SendMail(subscriptionInfo);
    }
}
