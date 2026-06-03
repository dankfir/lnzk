package com.tencent.wxcloudrun.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户订阅
 */
@Data
public class UserSubscription implements Serializable {
    private Integer id;
    private Integer userId;
    private String regions;       // 逗号分隔
    private String categories;
    private String keywords;
    private Integer pushEnabled;  // 1开 0关
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
