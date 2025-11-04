package com.riven.common.cron;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class CronExpressionParser {

    private static final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);;
    private static final CronParser parser = new CronParser(cronDefinition);

    public static ZonedDateTime getNextExecutionZonedDateTime(String cronExpression) {
        Cron cron = parser.parse(cronExpression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        return executionTime.nextExecution(ZonedDateTime.now()).orElse(null);
    }



    public static Date getNextExecutionDate(String cronExpression) {
        try {
            ZonedDateTime nextExecutionTime = getNextExecutionZonedDateTime(cronExpression);
            if (nextExecutionTime != null) {
                // �?ZonedDateTime 转换�?Instant
                Instant instant = nextExecutionTime.toInstant();
                // �?Instant 转换�?Date
                return Date.from(instant);
            }
        } catch (Exception e) {
           log.error("转型异常",e);

        }
        return null;
    }


    /**
     * 获取指定时间段内所有的 cron 表达式匹配时间点�?
     *
     * @param cronExpression cron 表达�?
     * @param startDate      开始时�?
     * @param endDate        结束时间
     * @return 匹配的时间点列表
     */
    public static List<ZonedDateTime> getCronExecutionTimes(String cronExpression, Date startDate, Date endDate) {
        List<ZonedDateTime> executionTimes = new ArrayList<>();

        if (startDate == null){
            return  new ArrayList<>();
        }

        try {
            Cron cron = parser.parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);

            // �?Date 转换�?ZonedDateTime
            ZonedDateTime startTime = startDate.toInstant().atZone(ZoneId.systemDefault());
            ZonedDateTime endTime = endDate.toInstant().atZone(ZoneId.systemDefault());

            ZonedDateTime nextExecution = executionTime.nextExecution(startTime).orElse(null);
            while (nextExecution != null && !nextExecution.isAfter(endTime)) {
                executionTimes.add(nextExecution);
                // 使用 Instant 进行时间增量，避免频繁创�?ZonedDateTime 对象
                nextExecution = ZonedDateTime.ofInstant(nextExecution.toInstant().plusSeconds(1), nextExecution.getZone());
                nextExecution = executionTime.nextExecution(nextExecution).orElse(null);
            }
        } catch (Exception e) {
            log.error("Error parsing or computing cron expression: {}", e.getMessage(), e);
        }

        return executionTimes;
    }


}
