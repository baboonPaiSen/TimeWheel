package com.riven.common.entity;

import lombok.Data;

import java.util.List;



@Data
public class SubscriptionItem {

    /**
     * 订阅类型
     */
    private String type;

    /**
     * 订阅名词
     */
    private String name;


    private List<SubscriptionReportItem> reports;


}
