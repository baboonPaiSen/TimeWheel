package com.riven.common.dto;

import lombok.Data;

@Data
public class CheckDataDTO {

    private String from = "redis";

    private String referType;

    public CheckDataDTO(String referType) {
        this.referType = referType;
    }
}
