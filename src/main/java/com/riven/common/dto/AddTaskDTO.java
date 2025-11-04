package com.riven.common.dto;


import com.riven.common.entity.SubscriptionInfo;
import com.riven.common.enums.*;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
@Slf4j
public class AddTaskDTO {


    private Long taskId;


    /**
     * 用户id
     */
    private Long userId;


    /**
     * cron表达式
     */
    private String cron;


    /**
     * 特殊表达式
     */
    private String specDateCron;


    /**
     * 个人参数
     */
    private String personalParam;


    /**
     * 引用类型
     */
    private String referType;


    /**
     * 发送类型
     */
    private Integer sendType;


    /**
     * 订阅类型
     */
    private String subscriptionType;

    /**
     * 发送给谁（个人邮箱）
     */
    private String toEmail;

    /**
     * 抄送给谁
     */
    private String ccEmail;

    /**
     * 由谁发送
     */
    private String fromEmail;


    private Date createDate;

    private Date updateDate;

    private String traceId;




    /**
     * 校验方法，确保所有字段都不为空
     */
    public void validate(OperType operType)  {

        if (StringUtils.isEmpty(traceId)){
            traceId  = UUID.randomUUID().toString().replace("-","");
        }
        List<String> errors = new ArrayList<>();

        if (OperType.ADD.equals(operType))  {

            validParam(errors);
        }


        if (OperType.UPDATE.equals(operType) )  {

            if (taskId == null) {
                errors.add("taskId不能为空");
            }

            validParam(errors);
        }


        if (OperType.DELETE.equals(operType))  {
            if (taskId == null) {
                errors.add("taskId不能为空");
            }
            if (userId == null) {
                errors.add("userId不能为空");
            }
        }

        if (OperType.SELECT.equals(operType)){
            if (userId == null && taskId ==null) {
                errors.add("userId和taskId不能全为空");
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(", ", errors));
        }
    }

    private void validParam(List<String> errors) {




        if (userId == null) {
            errors.add("用户ID不能为空");
        }



        if (personalParam == null || personalParam.isEmpty()) {
            errors.add("个人参数不能为空");
        }


        if (subscriptionType ==null || subscriptionType.isEmpty() || !CodeEnum.isValidCode(SubscriptionType.class,this.getSubscriptionType())) {
            errors.add("订阅类型不能为空");

        }else {
            if (SubscriptionType.EMAIL.getCode().equals(this.getSubscriptionType())) {


                if (sendType == null|| !CodeEnum.isValidCode(SendType.class,this.getSendType())) {
                    errors.add("发送类型不能为空");
                }

                if ((cron == null || cron.isEmpty()) && (specDateCron == null || specDateCron.isEmpty())) {
                    errors.add("Cron表达式/specCron不能全为空");
                }

                if (StringUtils.isEmpty(cron)){
                    cron = timeToCronExpression(specDateCron);
                }

                if (toEmail == null || toEmail.isEmpty()) {
                    errors.add("收件人不能为空");
                }

            }
        }
    }


    // 正则表达式，用于匹配 HH:mm:ss 格式的字符串
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})$");

    // 正则表达式，用于匹配每天定时执行的 cron 表达式
    private static final Pattern CRON_PATTERN = Pattern.compile("^0 (\\d{2}) (\\d{2}) \\* \\* \\?$");

    /**
     * 将 HH:mm:ss 格式的字符串转换为每天定时执行的 cron 表达式
     *
     * @param timeStr 时间字符串，格式为 HH:mm:ss
     * @return 每天定时执行的 cron 表达式
     */
    public static String timeToCronExpression(String timeStr) {
        Matcher matcher = TIME_PATTERN.matcher(timeStr);
        if (!matcher.matches()) {
            throw new RuntimeException("Invalid time format. Expected format: HH:mm:ss");
        }

        String hour = matcher.group(1);
        String minute = matcher.group(2);
        String second = matcher.group(3);
        return String.format("0 %s %s * * ?", minute, hour);
    }

    /**
     * 将每天定时执行的 cron 表达式转换为 HH:mm:ss 格式的字符串
     *
     * @param cronExpression 每天定时执行的 cron 表达式
     * @return HH:mm:ss 格式的字符串
     */
    public static String cronExpressionToTime(String cronExpression) {
        Matcher matcher = CRON_PATTERN.matcher(cronExpression);
        if (!matcher.matches()) {
            throw new RuntimeException("Invalid cron expression. Expected format: 0 mm hh * * ?");
        }

        String minute = matcher.group(1);
        String hour = matcher.group(2);
        return String.format("%s:%s:00", hour, minute);
    }



    public static AddTaskDTO convert(SubscriptionInfo find) {
        AddTaskDTO addTaskDTO = new AddTaskDTO();
        addTaskDTO.setTaskId(find.getId());
        addTaskDTO.setUserId(find.getUserId());
        addTaskDTO.setCron(find.getCronExpression());
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(find.getCronExpression())){
            addTaskDTO.setSpecDateCron(AddTaskDTO.cronExpressionToTime(find.getCronExpression()));

        }
        addTaskDTO.setPersonalParam(find.getPersonalParam());
        addTaskDTO.setReferType(find.getReferType());
        addTaskDTO.setSendType(find.getSendType());
        addTaskDTO.setSubscriptionType(find.getSubscriptionType());
        addTaskDTO.setCcEmail(find.getCcEmail());
        addTaskDTO.setFromEmail(find.getFromEmail());
        addTaskDTO.setToEmail(find.getToEmail());
        addTaskDTO.setCreateDate(find.getCreatedAt());
        addTaskDTO.setUpdateDate(find.getUpdatedAt());
        addTaskDTO.setTraceId(UUID.randomUUID().toString().replace("-",""));
        return addTaskDTO;
    }


    @Override
    public String toString() {
        return "AddTaskDTO{" +
                "taskId=" + taskId +
                ", userId=" + userId +
                ", cron='" + cron + '\'' +
                ", specDateCron='" + specDateCron + '\'' +
                ", personalParam='" + personalParam + '\'' +
                ", referType='" + referType + '\'' +
                ", sendType=" + sendType +
                ", subscriptionType='" + subscriptionType + '\'' +
                ", toEmail='" + toEmail + '\'' +
                ", ccEmail='" + ccEmail + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                '}';
    }

}