package com.riven.common.entity;

import java.util.Date;

/**
 * 个人订阅信息表
 */
public class SubscriptionInfo {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 发送类型
     */
    private Integer sendType;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 引用类型
     */
    private String referType;

    /**
     * 个人参数
     */
    private String personalParam;

    /**
     * 下次执行时间
     */
    private Date nextExecutionTime;

    /**
     * 上次执行时间
     */
    private Date lastExecutionTime;

    /**
     * 订阅类型
     */
    private String subscriptionType;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 发送给谁（个人邮箱）
     */
    private String toEmail;

    /**
     * 抄送给谁
     */
    private String ccEmail;

    /**
     * 由谁发送
     */
    private String fromEmail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSendType() {
        return sendType;
    }

    public void setSendType(Integer sendType) {
        this.sendType = sendType;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getReferType() {
        return referType;
    }

    public void setReferType(String referType) {
        this.referType = referType;
    }

    public String getPersonalParam() {
        return personalParam;
    }

    public void setPersonalParam(String personalParam) {
        this.personalParam = personalParam;
    }

    public Date getNextExecutionTime() {
        return nextExecutionTime;
    }

    public void setNextExecutionTime(Date nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    public Date getLastExecutionTime() {
        return lastExecutionTime;
    }

    public void setLastExecutionTime(Date lastExecutionTime) {
        this.lastExecutionTime = lastExecutionTime;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getCcEmail() {
        return ccEmail;
    }

    public void setCcEmail(String ccEmail) {
        this.ccEmail = ccEmail;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
}