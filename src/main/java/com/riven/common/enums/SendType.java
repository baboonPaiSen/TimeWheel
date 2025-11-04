package com.riven.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;


@Getter
@AllArgsConstructor
public enum SendType  implements CodeEnum<Integer>{

    SCHEDULED(0),
    REALTIME(1)


    ;

    private Integer code;

}
