package com.riven.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Switch  implements CodeEnum<String>{

    ON("on"),
    OFF("off")

    ;


    private String code;

}
