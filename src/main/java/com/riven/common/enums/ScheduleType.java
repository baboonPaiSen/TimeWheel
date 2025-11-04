package com.riven.common.enums;

public enum ScheduleType {
    TIME_WHEEL("time_wheel"),
    DELAY_QUEUE("delay_queue");

    private final String value;

    ScheduleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ScheduleType fromValue(String value) {
        for (ScheduleType type : ScheduleType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new RuntimeException("Unknown schedule type: " + value);
    }
}
