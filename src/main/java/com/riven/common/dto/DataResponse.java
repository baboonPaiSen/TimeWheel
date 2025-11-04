package com.riven.common.dto;

import lombok.Data;

@Data
public class DataResponse {

    private Integer code;
    
    private String msg;
    
    private SubscribeMailDTO data;
}
