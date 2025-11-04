package com.riven.common.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionReportItem {


    /**
     * 研报标题
     */
    private String reportTitle;

    /**
     * 研报时间
     */
    private String reportDate;

    /**
     * 研报来源
     */
    private String reportSource;

    /**
     * 研报作者
     */
    private String reportAuthor;
    /**
     * 研报类别
     */
    private String reportCategory;

    /**
     * 研报摘要
     */
    private String abstractText;


    /**
     * 研报id
     */
    private Long id;


}
