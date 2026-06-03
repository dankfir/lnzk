package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.WxUser;

import java.util.Optional;

public interface WxUserService {

    /**
     * 微信登录：code换取openid，新用户自动注册
     */
    WxUser login(String code, String nickname, String avatarUrl);

    Optional<WxUser> getById(Integer id);

    Optional<WxUser> getByOpenid(String openid);
}
