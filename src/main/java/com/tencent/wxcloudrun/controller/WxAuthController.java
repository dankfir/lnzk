package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.LoginRequest;
import com.tencent.wxcloudrun.model.WxUser;
import com.tencent.wxcloudrun.service.WxUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信登录接口
 */
@RestController
@RequestMapping("/api/auth")
public class WxAuthController {

    final WxUserService wxUserService;
    final Logger logger = LoggerFactory.getLogger(WxAuthController.class);

    public WxAuthController(@Autowired WxUserService wxUserService) {
        this.wxUserService = wxUserService;
    }

    /** 微信登录 */
    @PostMapping("/login")
    ApiResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            WxUser user = wxUserService.login(request.getCode(), request.getNickname(), request.getAvatarUrl());
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getId());
            result.put("nickname", user.getNickname());
            result.put("avatarUrl", user.getAvatarUrl());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
