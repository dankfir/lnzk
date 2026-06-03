package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.SubscriptionRequest;
import com.tencent.wxcloudrun.model.UserSubscription;
import com.tencent.wxcloudrun.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 用户订阅接口
 */
@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    final SubscriptionService subscriptionService;

    public SubscriptionController(@Autowired SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /** 获取当前订阅 */
    @GetMapping("/{userId}")
    ApiResponse get(@PathVariable Integer userId) {
        Optional<UserSubscription> sub = subscriptionService.getByUserId(userId);
        return sub.map(ApiResponse::ok).orElseGet(() -> ApiResponse.ok(null));
    }

    /** 保存或更新订阅 */
    @PostMapping("/{userId}")
    ApiResponse save(@PathVariable Integer userId, @RequestBody SubscriptionRequest req) {
        UserSubscription sub = subscriptionService.saveOrUpdate(
                userId, req.getRegions(), req.getCategories(),
                req.getKeywords(), req.getPushEnabled());
        return ApiResponse.ok(sub);
    }

    /** 取消订阅 */
    @DeleteMapping("/{userId}")
    ApiResponse disable(@PathVariable Integer userId) {
        subscriptionService.disable(userId);
        return ApiResponse.ok();
    }
}
