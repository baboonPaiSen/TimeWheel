package com.riven.common.entity;


import lombok.Data;

import java.util.List;

@Data
public class MailSendEntity {

    //标题
    private String subject;


    //正文
    private String body;



    //发送给谁
    private List<String> toMail;


    //抄送
    private List<String> cc;

    private Long  userId;

    /**
     * 是否无实际内容，无需发送
     */
    private Boolean pass = false;

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public void setToMail(List<String> toMail) {
        this.toMail = toMail;
    }
}