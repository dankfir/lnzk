package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信用户
 */
@Data
public class WxUser implements Serializable {
    private Integer id;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
