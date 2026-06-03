package com.tencent.wxcloudrun.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.dao.WxUserMapper;
import com.tencent.wxcloudrun.model.WxUser;
import com.tencent.wxcloudrun.service.WxUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class WxUserServiceImpl implements WxUserService {

    final WxUserMapper wxUserMapper;
    final Logger logger = LoggerFactory.getLogger(WxUserServiceImpl.class);
    final RestTemplate restTemplate = new RestTemplate();
    final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wx.miniapp.appid}")
    private String appid;

    @Value("${wx.miniapp.secret}")
    private String secret;

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    public WxUserServiceImpl(@Autowired WxUserMapper wxUserMapper) {
        this.wxUserMapper = wxUserMapper;
    }

    @Override
    public WxUser login(String code, String nickname, String avatarUrl) {
        // 1. 调用微信 code2session
        String openid = code2session(code);
        if (openid == null) {
            throw new RuntimeException("微信登录失败：code无效或已过期");
        }

        // 2. 查找或创建用户
        WxUser user = wxUserMapper.getByOpenid(openid);
        if (user == null) {
            user = new WxUser();
            user.setOpenid(openid);
            user.setNickname(nickname != null ? nickname : "微信用户");
            user.setAvatarUrl(avatarUrl);
            wxUserMapper.insert(user);
            logger.info("新用户注册: openid={}", openid);
        } else {
            // 更新昵称头像
            if (nickname != null) {
                user.setNickname(nickname);
            }
            if (avatarUrl != null) {
                user.setAvatarUrl(avatarUrl);
            }
            wxUserMapper.updateLoginTime(user.getId());
            logger.info("用户登录: openid={}", openid);
        }

        return user;
    }

    private String code2session(String code) {
        try {
            String url = String.format(CODE2SESSION_URL, appid, secret, code);
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(resp);

            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                logger.error("code2session失败: {}", resp);
                return null;
            }
            return json.get("openid").asText();
        } catch (Exception e) {
            logger.error("code2session异常: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<WxUser> getById(Integer id) {
        return Optional.empty(); // 暂不单独查询
    }

    @Override
    public Optional<WxUser> getByOpenid(String openid) {
        return Optional.ofNullable(wxUserMapper.getByOpenid(openid));
    }
}
