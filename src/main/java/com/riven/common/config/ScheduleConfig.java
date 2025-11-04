package com.riven.common.config;

import com.riven.common.enums.ScheduleType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "start")
public class ScheduleConfig {

    private ScheduleType type;


    public ScheduleType getType() {
        return type;
    }

    public void setType(ScheduleType type) {
        this.type = type;
    }

}
