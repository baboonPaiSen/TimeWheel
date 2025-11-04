package com.riven.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatus implements CodeEnum<String> {



    WAIT("1000_wait"),
    CANCEL("2000_cancel"),
    RUNNING("3000_running");


    private final String code;



}
