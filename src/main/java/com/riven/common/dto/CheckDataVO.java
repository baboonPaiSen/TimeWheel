package com.riven.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckDataVO {

    private String clientId;

    private List<AddTaskDTO> data;
}
