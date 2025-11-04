package com.riven.common.mapper;

import com.riven.common.entity.SubscriptionInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SubscriptionInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SubscriptionInfo record);

    int insertSelective(SubscriptionInfo record);

    SubscriptionInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SubscriptionInfo record);

    int updateByPrimaryKey(SubscriptionInfo record);

    List<SubscriptionInfo> selectAllByUserId(@Param("userId") Long userId);

    List<SubscriptionInfo> selectAllByClientIdAndSubscriptionType(@Param("clientId") String clientId, @Param("subscriptionType") String subscriptionType);

    List<SubscriptionInfo> selectAllById(@Param("id") Long id);

    List<SubscriptionInfo> searchAllByReferTypeAndBusinessType(@Param("referType") String referType, @Param("businessType") String businessType);

    List<SubscriptionInfo> selectAllByReferTypeAndUserId(@Param("referType") String referType, @Param("userId") Long userId);
}
