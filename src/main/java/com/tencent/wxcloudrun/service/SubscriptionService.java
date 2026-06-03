package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.UserSubscription;

import java.util.Optional;

public interface SubscriptionService {

    Optional<UserSubscription> getByUserId(Integer userId);

    UserSubscription saveOrUpdate(Integer userId, String regions, String categories,
                                   String keywords, Integer pushEnabled);

    /** 取消订阅 */
    void disable(Integer userId);
}
