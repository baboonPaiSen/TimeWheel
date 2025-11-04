package com.riven.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReferType  implements CodeEnum<String> {

    PERSONAL("personal"),
    INDIVIDUAL("individual"),
    PRIVATE("private"),
    LOCAL("local"),
    ;
    

    private String code;

}