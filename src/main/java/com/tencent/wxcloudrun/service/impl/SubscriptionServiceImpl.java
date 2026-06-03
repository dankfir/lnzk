package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.UserSubscriptionMapper;
import com.tencent.wxcloudrun.model.UserSubscription;
import com.tencent.wxcloudrun.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    final UserSubscriptionMapper subscriptionMapper;

    public SubscriptionServiceImpl(@Autowired UserSubscriptionMapper subscriptionMapper) {
        this.subscriptionMapper = subscriptionMapper;
    }

    @Override
    public Optional<UserSubscription> getByUserId(Integer userId) {
        return Optional.ofNullable(subscriptionMapper.getByUserId(userId));
    }

    @Override
    public UserSubscription saveOrUpdate(Integer userId, String regions, String categories,
                                          String keywords, Integer pushEnabled) {
        UserSubscription exist = subscriptionMapper.getByUserId(userId);
        if (exist != null) {
            exist.setRegions(regions);
            exist.setCategories(categories);
            exist.setKeywords(keywords);
            exist.setPushEnabled(pushEnabled != null ? pushEnabled : 1);
            subscriptionMapper.update(exist);
            return exist;
        } else {
            UserSubscription sub = new UserSubscription();
            sub.setUserId(userId);
            sub.setRegions(regions);
            sub.setCategories(categories);
            sub.setKeywords(keywords);
            sub.setPushEnabled(pushEnabled != null ? pushEnabled : 1);
            subscriptionMapper.insert(sub);
            return sub;
        }
    }

    @Override
    public void disable(Integer userId) {
        UserSubscription exist = subscriptionMapper.getByUserId(userId);
        if (exist != null) {
            exist.setPushEnabled(0);
            subscriptionMapper.update(exist);
        }
    }
}
