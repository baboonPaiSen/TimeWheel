package com.riven.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SubscriptionType implements CodeEnum<String> {

    DEFAULT("default" ),
    EMAIL("email");

    private final String code;


}
