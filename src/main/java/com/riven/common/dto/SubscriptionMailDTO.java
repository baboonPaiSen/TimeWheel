package com.riven.common.dto;

import com.riven.common.entity.SubscriptionItem;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionMailDTO {


    private String emailTitle;
    private String title;
    private String subTitle;
    private List<SubscriptionItem> subscriptionList;
    private String emailExplanation;

}
