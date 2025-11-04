package com.riven.common.job;



import com.alibaba.fastjson.JSON;

import com.riven.common.config.RetryPolicy;
import com.riven.common.dto.AddTaskDTO;
import com.riven.common.dto.DataResponse;
import com.riven.common.dto.SubscribeMailDTO;
import com.riven.common.entity.MailSendEntity;
import com.riven.common.entity.SubscriptionInfo;
import com.riven.common.enums.ExpoMappingEnum;
import com.riven.common.service.RetryCallback;
import com.riven.common.service.RetryService;
import com.riven.common.service.TextTemplateService;
import com.riven.common.utils.ErrorLogUtils;
import com.riven.common.utils.MailSendUtils;
import com.riven.common.utils.SpringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
public class SendMail implements Runnable {

    public static final int MILLI_SECONDS = 10000;

    private Long taskId;

    private String toMail;

    private String content;

    private String subject;

    private String from;

    private String cc;

    private Long userId;

    private Object pullParam;

    private String personalParam;

    private String referType;


    public SendMail(SubscriptionInfo subscriptionInfo) {
        this.taskId = subscriptionInfo.getId();
        this.toMail = subscriptionInfo.getToEmail();
        this.cc = subscriptionInfo.getCcEmail();
        this.userId = subscriptionInfo.getUserId();
        this.personalParam = subscriptionInfo.getPersonalParam();
        this.referType = subscriptionInfo.getReferType();
        this.pullParam = AddTaskDTO.convert(subscriptionInfo);
    }


    @Override
    public void run() {

        if (!StringUtils.isEmpty(toMail)) {


            try {
                MailSendEntity mailSendEntity = fillParam();
                if (mailSendEntity.getPass()){
                    return;
                }
                MailSendUtils.sendMail(mailSendEntity);
            } catch (Exception e) {
                log.error("执行发送邮件时异常,开启最大重试4次，第一次立即重试,后续初始延迟1分，每次延迟5倍补偿策略，如果还未执行成功, 放弃该任务, e={}", e.getMessage(), e);
                RetryPolicy policy = new RetryPolicy(4, 60000, 5.0);
                RetryService retryService = new RetryService(policy, taskId);
                RetryCallback callback = () -> {
                    log.error("执行异常回调流程,当前时间{},mail={} ", new DateTime().toString("yyyy-MM-dd HH:mm:ss"), this);
                    if (!StringUtils.isEmpty(toMail)) {
                        MailSendEntity retry = fillParam();
                        if (retry.getPass()){
                            return;
                        }
                        MailSendUtils.sendMail(retry);

                    }

                };
                retryService.executeWithRetry(callback);

            }
        }

    }

    private MailSendEntity fillParam() {
        MailSendEntity mailSendEntity = new MailSendEntity();
        List<String> toList = Arrays.stream(toMail.split(",")).collect(Collectors.toList());
        if (!StringUtils.isEmpty(cc)) {
            List<String> ccList = Arrays.stream(cc.split(",")).collect(Collectors.toList());
            mailSendEntity.setCc(ccList);
        }

        String content = "个人订阅内容";

        String subject = "个人订阅邮件";



        String resp= "" ;
        //10秒
        try {

            log.info("获取个人邮件模板,pullParam={}", JSON.toJSONString(pullParam));



        } catch (Exception e) {

            //邮件预警
            notify(resp, e,this.pullParam);
            throw new RuntimeException("获取邮箱内容失败", e);

        }


        mailSendEntity.setSubject(subject);
        mailSendEntity.setBody(content);

        mailSendEntity.setUserId(userId);
        mailSendEntity.setToMail(toList);
        return mailSendEntity;
    }

    private void notify(String resp, Exception e, Object pullParam) {
        try {
            MailSendEntity toMySelf = new MailSendEntity();
            toMySelf.setSubject("个人订阅邮件发送失败");
            List<String> results = new ArrayList<>();
            results.add("当前环境:"+ SpringUtil.getProfile());
            results.add("请求信息:"+this);
            results.add("响应信息:"+ resp);
            results.add("错误信息:"+ErrorLogUtils.getErrorMsg(e, 100));
            Map<String, Object> variables = new HashMap<>();
            variables.put("result", results);

            TextTemplateService textTemplateService = SpringUtil.getBean(TextTemplateService.class);
            String html = textTemplateService.process("noticeEmail.ftl", variables);
            toMySelf.setBody(html);
            List<String> notifyListByPullParam = null;
            try {
                notifyListByPullParam = ExpoMappingEnum.getNotifyListByPullParam(pullParam);
            } catch (Exception ex) {
                log.error("查询到无提醒收件人,直接忽略", ex);
               return;
            }
            toMySelf.setToMail(notifyListByPullParam);
            toMySelf.setUserId(userId);
            MailSendUtils.sendMail(toMySelf);
        } catch (Exception exception) {
            log.error("发送邮件预警邮件失败", exception);
        }
    }


    @Override
    public String toString() {
        return "SendMail{" +
                "taskId=" + taskId +
                ", toMail='" + toMail + '\'' +
                ", content='" + content + '\'' +
                ", subject='" + subject + '\'' +
                ", from='" + from + '\'' +
                ", cc='" + cc + '\'' +
                ", userId=" + userId +
                ", param=" + pullParam +
                ", referType='" + referType + '\'' +
                '}';
    }
}